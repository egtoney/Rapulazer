import glob
from random import shuffle

import sys
sys.path.append('../')
import definition

csv_base = definition.csv_base

files = glob.glob(csv_base + '*.csv')

dataset = []

for _file in files:
	with open(_file, 'r') as f:
		for line in f:
			dataset.append(line)

shuffle(dataset)

train_split = dataset[:int(len(dataset) * 0.8)]
test_split = dataset[int(len(dataset) * 0.8):]

with open('./train.csv', 'w') as f:
	for line in train_split:
		f.write(line)

with open('./test.csv', 'w') as f:
	for line in test_split:
		f.write(line)
