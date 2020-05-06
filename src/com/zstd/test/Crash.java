package com.zstd.test;

import com.github.luben.zstd.ZstdOutputStream;
import com.google.common.util.concurrent.ThreadFactoryBuilder;


import java.io.*;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

public class Crash {
	static ExecutorService zstdPool = Executors.newFixedThreadPool(10,
			new ThreadFactoryBuilder().setNameFormat("ZSTD-pool-%d").build());

	public static int maxLinePerStream = 100000;
	public static int maxTasks = 10000;

	public static void main(String args[]) throws InterruptedException {

		AtomicInteger atomicInteger = new AtomicInteger(0);
		AtomicInteger successfulCompletedTask = new AtomicInteger(0);

		for (int k = 0; k < maxTasks; k++) {
			Future<?> submit = zstdPool.submit(new Runnable() {

				@Override
				public void run() {
					System.out.println(
							"Started write for :" + atomicInteger.get() + "_" + Thread.currentThread().getId());

					try {
						DataOutputStream dos = null;
						ByteArrayOutputStream bOutput = null;
						try {

							bOutput = new ByteArrayOutputStream(maxLinePerStream * 30) {
								boolean closed = false;

								@Override
								public void close() throws IOException {
									this.closed = true;
									super.close();
								}

								@Override
								public synchronized void write(byte[] b, int off, int len) {
//                                    if (closed) {
//                                        throw new RuntimeException("Stream already closed");
//                                    }
									super.write(b, off, len);
								}

								@Override
								public synchronized void write(int b) {
//                                    if (closed) {
//                                        throw new RuntimeException("Stream already closed");
//                                    }
									super.write(b);
								}

								@Override
								public void flush() throws IOException {
									if (closed) {
										throw new RuntimeException("Stream already closed");
									}
									super.flush();
								}
							};

							BufferedOutputStream bos = new BufferedOutputStream(bOutput);

							dos = new DataOutputStream(new BufferedOutputStream(new ZstdOutputStream(bos, 1), 32768));

							Random r1 = new Random();
							int i = maxLinePerStream;
							while (i-- > 0) {
								dos.writeLong(r1.nextLong());
								dos.writeLong(r1.nextInt());
								if (i % 1000 == 0) {
									bos.close();
								}

							}

							System.out.println("Completed write for :" + atomicInteger.get() + "_"
									+ Thread.currentThread().getId());

						} catch (Throwable e) {
							try {
								if (dos != null) {
									dos.close();
								}
							} catch (Throwable e1) {
								// e1.printStackTrace();
							}

							throw new RuntimeException(e);
						}
					} catch (Throwable th1) {
						// th1.printStackTrace();
					} finally {
						successfulCompletedTask.incrementAndGet();
					}
				}
			});
		}

		System.out.println("Total task completed : " + successfulCompletedTask.get() + " Out of Task : " + maxTasks);
		Thread.sleep(10000000L);

	}
}
