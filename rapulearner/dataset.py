import time
import tensorflow as tf
import wave
import numpy as np
import definition

d = 400000

def read_and_decode(recname):

  def read_mp3(f):
    try:
      fid = wave.open(f, 'rb')
      raw = fid.readframes(fid.getnframes())
      y = np.fromstring(raw,dtype=np.int16).astype(np.float32)

      # this is now handled as a pre-processing step
      # y = signal.decimate(y,2).astype(np.float32)

      # pad if necessary 
      amount_short = d - y.size
      if 0 < amount_short:
          y = np.pad(y, 
                  (0,amount_short),
                  'wrap') 

      y = y / 32768.
      #y = y / np.sqrt(1e-8 + np.mean(y**2))
      #y = y / 100.

      return y

    except Exception,e:
        print(e)

  y = tf.py_func(read_mp3, [recname], [tf.float32])
  y = tf.reshape(y,(-1,1,1))
  y = tf.random_crop(y,(d,1,1)) 
  y = tf.squeeze(y)

  return y 

def load_csv(fname, num_epochs=None):

  fq = tf.train.string_input_producer([fname], num_epochs=num_epochs) 
  reader = tf.TextLineReader()
  key, value = reader.read(fq)
  recname, label = tf.decode_csv(value,
                                 record_defaults=[['missing'],[0]],
                                 field_delim='|')

  feat = read_and_decode(recname)

  return feat, label, recname

def load_train():
	with tf.device('/cpu:0'):
		data = load_csv('./dataset/train.csv')

		tensors = tf.train.shuffle_batch(data,
																		 num_threads=32,
																		 capacity=1000,
																		 min_after_dequeue=200,
																		 batch_size=32)

		return tensors

def load_test():
	with tf.device('/cpu:0'):
		data = load_csv('./dataset/test.csv', num_epochs=1)

		tensors = tf.train.batch(data,
														 batch_size=32,
														 num_threads=16,
														 capacity=1000,
														 allow_smaller_final_batch=True)

		return tensors
