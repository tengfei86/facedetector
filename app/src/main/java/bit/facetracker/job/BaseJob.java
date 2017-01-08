package bit.facetracker.job;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.birbit.android.jobqueue.Job;
import com.birbit.android.jobqueue.Params;
import com.birbit.android.jobqueue.RetryConstraint;


/**
 * Created by blade on 2/18/16.
 */
public class BaseJob extends Job {
    protected static final int PRIORITY = 1;

    public static final int NEW = 0;
    public static final int OLD = 1;

    protected BaseJob(Params params) {
        super(params);
    }

    /**
     * Called when the job is added to disk and committed.
     * This means job will eventually run. This is a good time to update local database and dispatch events.
     * <p>
     * Changes to this class will not be preserved if your job is persistent !!!
     * <p>
     * Also, if your app crashes right after adding the job, {@code onRun} might be called without an {@code onAdded} call
     * <p>
     * Note that this method is called on JobManager's thread and will block any other action so
     * it should be fast and not make any web requests (File IO is OK).
     */
    @Override
    public void onAdded() {

    }

    /**
     * The actual method that should to the work.
     * It should finish w/o any exception. If it throws any exception,
     * {@link #shouldReRunOnThrowable(Throwable, int, int)} will be called to
     * decide either to dismiss the job or re-run it.
     *
     * @throws Throwable Can throw and exception which will mark job run as failed
     */
    @Override
    public void onRun() throws Throwable {

    }

    /**
     * Called when a job is cancelled.
     *
     * @param cancelReason It is one of:
     *                     <ul>
     *                     <li>{@link CancelReason#REACHED_RETRY_LIMIT}</li>
     *                     <li>{@link CancelReason#CANCELLED_VIA_SHOULD_RE_RUN}</li>
     *                     <li>{@link CancelReason#CANCELLED_WHILE_RUNNING}</li>
     *                     <li>{@link CancelReason#SINGLE_INSTANCE_WHILE_RUNNING}</li>
     *                     <li>{@link CancelReason#SINGLE_INSTANCE_ID_QUEUED}</li>
     *                     </ul>
     * @param throwable    The exception that was thrown from the last execution of {@link #onRun()}
     */
    @Override
    protected void onCancel(int cancelReason, @Nullable Throwable throwable) {

    }

    /**
     * If {@code onRun} method throws an exception, this method is called.
     * <p>
     * If you simply want to return retry or cancel, you can use {@link RetryConstraint#RETRY} or
     * {@link RetryConstraint#CANCEL}.
     * <p>
     * You can also use a custom {@link RetryConstraint} where you can change the Job's priority or
     * add a delay until the next run (e.g. exponential back off).
     * <p>
     * Note that changing the Job's priority or adding a delay may alter the original run order of
     * the job. So if the job was added to the queue with other jobs and their execution order is
     * important (e.g. they use the same groupId), you should not change job's priority or add a
     * delay unless you really want to change their execution order.
     *
     * @param throwable   The exception that was thrown from {@link #onRun()}
     * @param runCount    The number of times this job run. Starts from 1.
     * @param maxRunCount The max number of times this job can run. Decided by {@link #getRetryLimit()}
     * @return A {@link RetryConstraint} to decide whether this Job should be tried again or not and
     * if yes, whether we should add a delay or alter its priority. Returning null from this method
     * is equal to returning {@link RetryConstraint#RETRY}.
     */
    @Override
    protected RetryConstraint shouldReRunOnThrowable(@NonNull Throwable throwable, int runCount, int maxRunCount) {
        return RetryConstraint.CANCEL;
    }

    protected BaseJob() {
        super(new Params(PRIORITY));
    }

    protected BaseJob(int priority) {
        super(new Params(priority).persist());
    }

    protected BaseJob(boolean isRequireNetwork) {
        super(new Params(PRIORITY).persist().setRequiresNetwork(isRequireNetwork));
    }


}
