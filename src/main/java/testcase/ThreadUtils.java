package testcase;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.List;
import java.util.concurrent.Future;

//
// copied from https://web.archive.org/web/20191105154937/http://nadeausoftware.com/articles/2008/04/java_tip_how_list_and_find_threads_and_thread_groups
//
public class ThreadUtils {
    ThreadGroup rootThreadGroup = null;

    public boolean hasActive(List<Future<?>> futures) {
        for (Future<?> future : futures) {
            if (!future.isDone()) {
                return true;
            }
        }
        return false;
    }

    public ThreadGroup getRootThreadGroup() {
        if (rootThreadGroup != null)
            return rootThreadGroup;
        ThreadGroup tg = Thread.currentThread().getThreadGroup();
        ThreadGroup ptg;
        while ((ptg = tg.getParent()) != null)
            tg = ptg;
        return tg;
    }

    public Thread[] getAllThreads() {
        final ThreadGroup root = getRootThreadGroup();
        final ThreadMXBean thbean = ManagementFactory.getThreadMXBean();
        int nAlloc = thbean.getThreadCount();
        int n = 0;
        Thread[] threads;
        do {
            nAlloc *= 2;
            threads = new Thread[nAlloc];
            n = root.enumerate(threads, true);
        } while (n == nAlloc);
        return java.util.Arrays.copyOf(threads, n);
    }

    public ThreadGroup[] getAllThreadGroups() {
        final ThreadGroup root = getRootThreadGroup();
        int nAlloc = root.activeGroupCount();
        int n = 0;
        ThreadGroup[] groups;
        do {
            nAlloc *= 2;
            groups = new ThreadGroup[nAlloc];
            n = root.enumerate(groups, true);
        } while (n == nAlloc);

        ThreadGroup[] allGroups = new ThreadGroup[n + 1];
        allGroups[0] = root;
        System.arraycopy(groups, 0, allGroups, 1, n);
        return allGroups;
    }

    public Thread[] getAllDaemonThreads() {
        final Thread[] allThreads = getAllThreads();
        final Thread[] daemons = new Thread[allThreads.length];
        int nDaemon = 0;
        for (Thread thread : allThreads)
            if (thread.isDaemon())
                daemons[nDaemon++] = thread;
        return java.util.Arrays.copyOf(daemons, nDaemon);
    }

    public Thread[] getAllThreads(final Thread.State state) {
        final Thread[] allThreads = getAllThreads();
        final Thread[] found = new Thread[allThreads.length];
        int nFound = 0;
        for (Thread thread : allThreads)
            if (thread.getState() == state)
                found[nFound++] = thread;
        return java.util.Arrays.copyOf(found, nFound);
    }
}
