package bit.facetracker.manager;

import android.app.Application;
import android.content.Context;

import com.birbit.android.jobqueue.Job;
import com.birbit.android.jobqueue.JobManager;
import com.birbit.android.jobqueue.config.Configuration;

/**
 * Created by blade on 2/17/16.
 */
public class JobQueueManager extends JobManager {

    private static JobQueueManager sJobManager;

    /**
     * Default constructor that will create a JobManager with 1 {@link SqliteJobQueue} and 1 {@link NonPersistentPriorityQueue}
     *
     * @param context job manager will use applicationContext.
     */
    private JobQueueManager(Context context) {
        super(new Configuration.Builder(context).build());
    }

    public static synchronized  JobQueueManager getJobManager(Application appcontext) {
        if (sJobManager == null) {
            sJobManager = new JobQueueManager(appcontext);
        }
        return sJobManager;
    }

    public void addJob(Job job) {
        sJobManager.addJobInBackground(job);
    }


}
