package galo.sample.wiki.search;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * A wrapper around a synchronized Collection Set of process Strings meant for referencing and
 * manipulating across multiple threads and simple calls not to bloat the user with many options.
 * Used for the sole task of managing processes, allowing one process to check it is has been removed
 * from the process map and another to have to ability to cancel operations by removing the process.
 *
 * This object usually deals with at most a two threads, one processing thread and the process owner
 * thread (from which it was started from).  More threads can certainly manipulate and look at its
 * component information, but then you may create ambiguity with trying to figure out whether the
 * process has been cancelled or finished.
 *
 * Created by Galo on 2/27/2015.
 */
public final class ProcessStateMap {
    private Set<String> mPSMap;

    ProcessStateMap()
    {
        this.mPSMap = Collections.synchronizedSet(new HashSet<String>());
    }

    /**
     * signifies the start of a process using the process id.
     * @param processId
     * The unique process to set as 'Running'.
     */
    public void addProcess(String processId)
    {
        mPSMap.add(processId);
    }

    /**
     * Signifies either the cancelation or the conclusion of a process, depends on the context.  The
     * process will use it to mark the process complete and whereas the owner or the process will use
     * this to mark the process as cancelled so that the running process can see that the process has
     * been cancelled by another thread.
     *
     * This is synchronized.
     *
     * @param processId
     * The unique process to set as 'Canceled' or 'Finished', depending on the context.
     * @return
     * Returns true if the operation has successfully changed to this state, false if it had previously
     * been removed.
     */
    public boolean removeProcess(String processId)
    {
        return mPSMap.remove(processId);
    }

    public boolean contains(String processId)
    {
        return mPSMap.contains(processId);
    }

    public void removeAllRunningProcesses()
    {
        mPSMap.clear();
    }
}
