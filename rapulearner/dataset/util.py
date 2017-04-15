import os
import fnmatch
import glob
import eyed3

# Turn off annoying warning msgs from eyeD3
eyed3.log.setLevel('ERROR')

# Grab all mp3 files in directory, recursively
def grab_mp3(directory):
  mp3_files = []

  for root, dirnames, filenames in os.walk(directory):
    for filename in fnmatch.filter(filenames, '*.mp3'):
      mp3_files.append(os.path.join(root, filename))

  return mp3_files

# return 1 if rap, 0 if non-rap, -1 if no tags
def tag_check(fname):
  try:
    af = eyed3.load(fname)

    rap_tags = ['rap', 'hip', 'hop']

    for tag in rap_tags:
      if tag in af.tag.genre.name.lower():
        return 1

    return 0

  except AttributeError:
    return -1
