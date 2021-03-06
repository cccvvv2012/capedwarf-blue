/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.jboss.capedwarf.log;

import java.util.concurrent.ConcurrentMap;
import java.util.logging.LogRecord;

import com.google.appengine.api.log.LogServiceFactory;
import org.jboss.capedwarf.common.app.Application;
import org.jboss.util.collection.ConcurrentReferenceHashMap;

/**
 * Logger.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 * @author <a href="mailto:marko.luksa@gmail.com">Marko Luksa</a>
 */
public class Logger {
    // leave this one to CRHM (intentionally not using ComponentRegistry)
    private static ConcurrentMap<ClassLoader, Logable> logs = new ConcurrentReferenceHashMap<ClassLoader, Logable>();

    public static void publish(LogRecord record) {
        ClassLoader cl = Application.getAppClassloader();
        LoggerLogable logable = new LoggerLogable();
        Logable previous = logs.putIfAbsent(cl, logable);
        if (previous == null) {
            logable.log(record);
        } else {
            previous.log(record);
        }
    }

    public static void flush() {
    }

    public static void close() throws SecurityException {
    }

    private static class LoggerLogable implements Logable {
        private volatile Logable logable;

        private Logable getLogable() {
            if (logable == null) {
                synchronized (this) {
                    if (logable == null) {
                        logable = ((CapedwarfLogService)LogServiceFactory.getLogService());
                    }
                }
            }
            return logable;
        }

        public void log(LogRecord record) {
            getLogable().log(record);
        }
    }
}
