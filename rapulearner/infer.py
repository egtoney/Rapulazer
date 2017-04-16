import os
import util
import wave
import network
import definition
import numpy as np
import tensorflow as tf
import tensorflow.contrib.slim

slim = tf.contrib.slim

args = util.parse_arguments()
run_name = util.run_name(args)

checkpoint_dir = definition.ckpt_base + run_name + '/'
log_dir = definition.log_base + run_name + '/'

f = '/media/wsong/Big_Stuff/Datasets/Rapulazer/Music/processed/01 Always Something_0.wav'

fid = wave.open(f, 'rb')
raw = fid.readframes(fid.getnframes())
y = np.fromstring(raw,dtype=np.int16).astype(np.float32)

# this is now handled as a pre-processing step
# y = signal.decimate(y,2).astype(np.float32)

# pad if necessary 
y = y / 32768.
#y = y / np.sqrt(1e-8 + np.mean(y**2))
#y = y / 100.

y = y[:400000]
y = np.squeeze(y)

with tf.variable_scope('Input'):
  print('Defining input pipeline')

  feat = tf.Variable(np.zeros((400000,)), dtype=tf.float32, name='input')
  # feat = tf.placeholder(tf.float32, shape=(400000,), name='input')
  print feat 

with tf.variable_scope('Predictor'):
  print('Defining prediction network')

  logits = network.network(feat, 
                           is_training=False, 
                           capacity=args.capacity,
                           capacity2=args.capacity2,
                           network='v5')

with tf.variable_scope('Output'):
  output = tf.Variable(logits, name='output')
  print output 

config = tf.ConfigProto()
config.gpu_options.allow_growth = True

variables_to_restore = slim.get_model_variables()

variables_to_restore = [ var for var in variables_to_restore
                                if 'Predictor' in var.op.name ]
variables_to_restore = { var.op.name : var
                            for var in variables_to_restore }

restorer = tf.train.Saver(variables_to_restore)

with tf.Session(config=config) as sess:

  ckpt = tf.train.get_checkpoint_state(checkpoint_dir)
  if ckpt and ckpt.model_checkpoint_path: 
    print('Restoring previous checkpoint')
    restorer.restore(sess, ckpt.model_checkpoint_path)

  sess.run(tf.local_variables_initializer())
  sess.run(tf.global_variables_initializer())


  ckpt = tf.train.get_checkpoint_state(checkpoint_dir)
  if ckpt and ckpt.model_checkpoint_path: 
    print('Restoring previous checkpoint')
    restorer.restore(sess, ckpt.model_checkpoint_path)

  _output = sess.run([output])

  print _output

  saver = tf.train.Saver()
  saver.save(sess, './infer/model.ckpt')
  saver.export_meta_graph('./infer/model.meta')
