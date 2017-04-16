import os
import shutil
import argparse
import numpy as np
import tensorflow as tf

def parse_arguments():
    parser = argparse.ArgumentParser()
    parser.add_argument('-c1', dest='capacity', type=float, default=1.0)
    parser.add_argument('-c2', dest='capacity2', type=float, default=1.0)

    parser.add_argument('--restart', dest='restart',
            action='store_true', default=False)

    parser.add_argument('--num_iter', dest='num_iter',
            action='store', type=int, default='30000')
    
    args = parser.parse_args()

    return args

def run_name(args):
    run_name = ''
    run_name += 'v5_'
    run_name += str(args.capacity) + '_'
    run_name += str(args.capacity2) + '_'
    run_name += str(args.num_iter)

    return run_name

def remove_run(run_name):
    if os.path.exists('./checkpoint/' + run_name):
        shutil.rmtree('./checkpoint/' + run_name)
    if os.path.exists('./logs/' + run_name):
        shutil.rmtree('./logs/' + run_name)
