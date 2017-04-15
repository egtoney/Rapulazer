import glob
import util
import numpy as np
from random import shuffle

import sys
sys.path.append('../')
import definition

audio_base = definition.audio_base

# Folders containing for sure rap, non-rap music
nonrap_base = audio_base + 'nonrap/'
rap_base = audio_base + 'rap/'

# Folder containing music that needs tag checking
pool_base = audio_base + 'pool/'

# Look through pool music files to find rap and non-rap music
pool_files = util.grab_mp3(pool_base)

rap_music = []
nonrap_music = []

for fname in pool_files:
  is_rap = util.tag_check(fname)

  if is_rap == 1:
    rap_music.append(fname)
  elif is_rap == 0:
    nonrap_music.append(fname)
  else:
    continue

# Add for sure rap and non-rap music to created list
rap_music.extend(util.grab_mp3(rap_base))
nonrap_music.extend(util.grab_mp3(nonrap_base))

# Add labels for rap and non-rap music
rap_data = [(fname, 1) for fname in rap_music]
nonrap_data = [(fname, 0) for fname in nonrap_music]

train_data = []
test_data = []

train_data.extend(rap_data[:int(len(rap_data) * 0.8)])
train_data.extend(nonrap_data[:int(len(nonrap_data) * 0.8)])

test_data.extend(rap_data[int(len(rap_data) * 0.8):])
test_data.extend(nonrap_data[int(len(nonrap_data) * 0.8):])

shuffle(train_data)
shuffle(test_data)

with open('./train.csv', 'w') as f:
  for pair in train_data:
    f.write('%s|%d\n' % (pair[0], pair[1]))

with open('./test.csv', 'w') as f:
  for pair in test_data:
    f.write('%s|%d\n' % (pair[0], pair[1]))
