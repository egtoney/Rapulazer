import os
import util
import network
import dataset
import definition
import tensorflow as tf

args = util.parse_arguments()
run_name = util.run_name(args)

if args.restart:
  print('Removing ' + run_name + ' run from before')
  util.remove_run(run_name)

checkpoint_dir = definition.ckpt_base + run_name + '/'
log_dir = definition.log_base + run_name + '/'

if not tf.gfile.Exists(checkpoint_dir):
  print('Making checkpoint dir')
  os.makedirs(checkpoint_dir)

if not tf.gfile.Exists(log_dir):
  print('Making log dir')
  os.makedirs(log_dir)

with tf.variable_scope('Input'):
  print('Defining input pipeline')

  feat, label, recname = dataset.load_train()

with tf.variable_scope('Predictor'):
  print('Defining prediction network')

  logits = network.network(feat, 
                           is_training=True, 
                           capacity=args.capacity,
                           capacity2=args.capacity2,
                           network='v5')

with tf.variable_scope('Loss'):
  print('Defining loss functions')

  loss_reg = tf.add_n(tf.get_collection(tf.GraphKeys.REGULARIZATION_LOSSES))
  loss_class = tf.nn.sparse_softmax_cross_entropy_with_logits(
          logits,
          label)

  prediction = tf.cast(tf.argmax(logits,1),dtype=tf.int32)

  loss_class = 10*tf.reduce_mean(loss_class)

  loss = loss_class + loss_reg 

with tf.variable_scope('Train'):
    print('Defining training methods')

    global_step = tf.Variable(0,name='global_step',trainable=False)
    learning_rate = tf.train.exponential_decay(definition.lr_rate,
                                               global_step,
                                               int(args.num_iter / 6),
                                               definition.gamma,
                                               staircase=True)
    optimizer = tf.train.AdamOptimizer(definition.lr_rate,epsilon=.1)
    train_op = optimizer.minimize(loss,global_step=global_step)

    acc = tf.contrib.metrics.accuracy(prediction,label)

    print('\nTraining for %d iterations' % args.num_iter)
    print('Learning rate: %f' % definition.lr_rate)
    print('Gamma: %f' % definition.gamma)
    print('Step size every %d\n' % int(args.num_iter / 6))

with tf.variable_scope('Summaries'):
    print('Defining summaries')

    tf.summary.scalar('loss_class', loss_class)
    tf.summary.scalar('loss_reg', loss_reg)
    tf.summary.scalar('loss', loss)
    tf.summary.scalar('learning_rate', learning_rate)
    tf.summary.scalar('accuracy', acc)

config = tf.ConfigProto()
config.gpu_options.allow_growth = True

with tf.Session(config=config) as sess:

  update_ops = tf.group(*tf.get_collection(tf.GraphKeys.UPDATE_OPS))

  summary_writer = tf.summary.FileWriter(log_dir, 
                                         sess.graph,
                                         flush_secs=5)
  summary = tf.summary.merge_all()
  
  coord = tf.train.Coordinator()
  threads = tf.train.start_queue_runners(coord=coord)
  sess.run(tf.global_variables_initializer())

  saver = tf.train.Saver()
  ckpt = tf.train.get_checkpoint_state(checkpoint_dir)

  if not args.restart:
    ckpt = tf.train.get_checkpoint_state(checkpoint_dir)
    if ckpt and ckpt.model_checkpoint_path: 
      print('Restoring previous checkpoint')
      saver.restore(sess, ckpt.model_checkpoint_path)

  _i = sess.run(global_step)

  print('Starting training')
  try:
    while _i < args.num_iter:

      _,_,_i, \
      _loss,_loss_reg,_loss_class,_acc, \
      _summary \
      = sess.run([
          train_op,
          update_ops,
          global_step,
          loss,
          loss_reg,
          loss_class,
          acc,
          summary
          ])

      print(str(_i) +' : lc ' + str(_loss_class) +' : lr ' +
              str(_loss_reg) + ' : l ' + str(_loss) + ' : a ' + str(_acc))

      summary_writer.add_summary(_summary, _i)
      summary_writer.flush()

      if _i % 100 == 0:
        print("saving total checkpoint")
        saver.save(sess, checkpoint_dir + 'model.ckpt', global_step=_i)

  finally:
    print('Cleaning up')
    coord.request_stop()
    coord.join(threads)
    print('Done')
