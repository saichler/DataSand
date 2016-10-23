/*
 * Copyright (c) 2016 DataSand,Sharon Aicler and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.datasand.codec.util;

import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author - Sharon Aicler (saichler@gmail.com)
 */
public abstract class ThreadNode extends Thread {

    private static final Logger LOG = LoggerFactory.getLogger(ThreadNode.class);
    private boolean running = false;
    private final List<ThreadNode> children = new ArrayList<>();

    public ThreadNode(ThreadNode parent, String name) {
        this.setName(name);
        if (parent != null) {
            parent.children.add(this);
            this.setDaemon(true);
        }
    }

    public final void run() {
        initialize();
        LOG.info(this.getName() + " was started.");
        try {
            this.running = true;
            while (running) {
                execute();
            }
        } catch (Exception e) {
            if (running) {
                LOG.error(this.getName() + " was unexpectly terminated", e);
            }
        }
        distruct();
        LOG.info(this.getName() + " was shutdown.");
    }

    public abstract void initialize();

    public abstract void execute() throws Exception;

    public abstract void distruct();

    public void shutdown() {
        this.running = false;
        for (ThreadNode child : children) {
            child.shutdown();
        }
    }

    public void start() {
        for (ThreadNode child : this.children) {
            child.start();
        }
        boolean allstarted = false;
        while (!allstarted) {
            allstarted = true;
            for (ThreadNode child : this.children) {
                if (!child.isRunning()) {
                    allstarted = false;
                    break;
                }
            }
            if (!allstarted) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                }
            }
        }

        super.start();
    }

    public final boolean isRunning() {
        return this.running;
    }
}
