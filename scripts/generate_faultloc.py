#!/usr/bin/python3

"""
Generate a faultloc file
"""

from argparse import ArgumentParser
import json
import logging
import os
from pybatfish.client.commands import *
from pybatfish.datamodel import HeaderConstraints, PathConstraints
from pybatfish.question.question import load_questions, list_questions
from pybatfish.question import bfq
from pybatfish.exception import BatfishException
import re
import shutil
import sys
import pygraphviz
import importlib
import collections
import ipaddress
import pprint
from collections import OrderedDict
import queue
import concurrent.futures
import threading

# Load questions
def init_questions(verbose=False):
    # Load questions
    try:
        load_questions()
    except Exception as ex:
        print(ex)
        sys.exit("Failed to load questions")

def snapshot_name(snapshot_path):
    # Auto-determine snapshot name
    name = os.path.split(snapshot_path)[1]
    if (name == ''):
        name = os.path.split(os.path.split(snapshot_path)[0])[1]
    return name

# Load snapshot
def init_snapshot(snapshot_path, network, reinit=False):
    # Initialize network snapshot
    name = snapshot_name(snapshot_path)
    try:
        bf_set_network(network)
        if reinit and name in bf_list_snapshots():
            bf_delete_snapshot(name)
        if name not in bf_list_snapshots():
            bf_init_snapshot(snapshot_path, name)
            check_parsing()
        else:
            bf_set_snapshot(name)
    except Exception as ex:
        print(ex)
        sys.exit("Failed to initialize network snapshot")

# Check for parsing errors
def check_parsing(verbose=False):
    # Determine which files did not completely parse
    parse_status = bfq.fileParseStatus().answer().frame()
    not_passed = parse_status[parse_status['Status'] != 'PASSED']
    if (len(not_passed) == 0):
        print("All files successfully parsed")
        return
    print("%d files failed to parse:" % (len(not_passed)))
    print(not_passed.loc[:, 'File_Name':'Status'])

    # Determine which lines failed to parse
    parse_warning = bfq.parseWarning().answer().frame()
    print("%d parsing errors occurred:" % (len(parse_warning)))
    if (verbose):
        for i,row in parse_warning.iterrows():
            print('%s:%d %s' % (row['Filename'], row['Line'], row['Text']))
            print(row['Comment'])
            print(row['Parser_Context'] + "\n")
    else:
        print(parse_warning.loc[:,'Filename':'Line'])

def load_changes(mofylog_path, verbose=False):
    """Load changes from mofy log"""
    changes = []
    with open(mofylog_path, 'r') as mofylog:
        for line in mofylog:
            if (not line.startswith('{')):
                continue
            change = json.loads(line)
            changes.append(change)
    return changes

def process_changes(changes, verbose=False):
    """Process a list of changes to generate the appropriate faultly predicate 
    labels"""
    faults = set()
    for change in changes:
        change_faults = process_change(change, verbose)
        if (verbose):
            print(change)
            print(change_faults)
        faults.update(change_faults)
    return faults

def process_change(change, verbose=False):
    """Process a change to generate the appropriate faulty predicate labels"""
    if ('AccessList' in change['stanzatype']):
        return process_accesslist_change(change, verbose)
    elif ('OSPFNetwork' == change['stanzatype']):
        return process_ospfnetwork_change(change, verbose)
    elif ('StaticRoute' == change['stanzatype']):
        return process_staticroute_change(change, verbose)
    return []

def process_accesslist_change(change, verbose=False):
    """Process an access list change to generate the appropriate faulty 
    predicate labels"""
    faults = []
    acl_name = change['stanzaname']
    hostname = change['hostname']
    interfaces = bfq.interfaceProperties(nodes=hostname,
            properties="((Incoming)|(Outgoing))_Filter_Name"
            ).answer().frame().sort_values(by=['Interface'])
    for i, row in interfaces.iterrows():
        interface = row['Interface'].interface
#        print('%s %s %s %s' % (hostname, interface, 
#                row['Incoming_Filter_Name'], row['Outgoing_Filter_Name']))
        if (row['Incoming_Filter_Name'] == acl_name):
            faults.append("ACLS_INBOUND %s %s null" % (hostname, interface))
        if (row['Outgoing_Filter_Name'] == acl_name):
            faults.append("ACLS_OUTBOUND %s %s null" % (hostname, interface))
    return faults

def process_ospfnetwork_change(change, verbose=False):
    """Process an OSPF network change to generate the appropriate faulty 
    predicate labels"""
    faults = []
    hostname = change['hostname']
    interfaces = bfq.interfaceProperties(nodes=hostname, 
            properties="OSPF_Enabled"
            ).answer().frame().sort_values(by=['Interface'])
    for i, row in interfaces.iterrows():
        interface = row['Interface'].interface
        faults.append("EXPORT %s %s OSPF" % (hostname, interface))
    # FIXME: Also include IMPORT for interfaces on which OSPF no longer runs
    # due to the change?
    return faults

def process_staticroute_change(change, verbose=False):
    """Process a static route change to generate the appropriate faulty 
    predicate labels"""
    faults = []
    hostname = change['hostname']
    # TODO
    return faults

def save_faults(faults, faultloc_path, verbose=False):
    with open(faultloc_path, 'w') as faultloc:
        for fault in sorted(faults):
            faultloc.write(fault+'\n')

def main():
    # Parse arguments
    arg_parser = ArgumentParser(description='Parse and analyze configs using Batfish')
    arg_parser.add_argument('-network', dest='network', action='store',
            required=True, help='Name of the network')
    arg_parser.add_argument('-snapshot', dest='snapshot_path', action='store',
            required=True, help='Path to original network snapshot; snapshot'
            + 'must follow the structure documented at https://github.com/'
            + 'batfish/batfish/wiki/Packaging-snapshots-for-analysis')
    arg_parser.add_argument('-mofylog', dest='mofylog_path', action='store', 
            required=True, help='Path to mofy change log')
    arg_parser.add_argument('-faultloc', dest='faultloc_path', action='store',
            default=None, help='Path to generated faultloc file')
    arg_parser.add_argument('-reinit', dest='reinit', action='store_true',
            default=False, help='Re-initialize snapshot if it already exists')
    arg_parser.add_argument('-verbose', dest='verbose', action='store_true',
            default=False, help='Display detailed output')
    settings = arg_parser.parse_args()

    logging.getLogger('pybatfish').setLevel(logging.WARN)

    # Load questions
    init_questions(settings.verbose)

    # Initialize snapshot
    init_snapshot(settings.snapshot_path, settings.network, settings.reinit)

    changes = load_changes(settings.mofylog_path, settings.verbose)
    faults = process_changes(changes, settings.verbose)
    for fault in sorted(faults):
        print(fault)
    if (settings.faultloc_path is not None):
        save_faults(faults, settings.faultloc_path, settings.verbose)

if __name__ == '__main__':
    main()
