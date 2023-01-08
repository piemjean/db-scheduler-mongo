package com.github.kagkarlsson.scheduler.utils;

import com.github.kagkarlsson.scheduler.task.CompletionHandler;
import com.github.kagkarlsson.scheduler.task.ExecutionContext;
import com.github.kagkarlsson.scheduler.task.TaskInstance;
import com.github.kagkarlsson.scheduler.task.VoidExecutionHandler;
import com.github.kagkarlsson.scheduler.task.helper.OneTimeTask;

public class TestTasks {

    public static final CompletionHandler<Void> REMOVE_ON_COMPLETE = new CompletionHandler.OnCompleteRemove<>();
    public static final VoidExecutionHandler<Void> DO_NOTHING = (taskInstance, executionContext) -> {};

    public static <T> OneTimeTask<T> oneTime(String name, Class<T> dataClass, VoidExecutionHandler<T> handler) {
        return new OneTimeTask<T>(name, dataClass) {
            @Override
            public void executeOnce(TaskInstance<T> taskInstance, ExecutionContext executionContext) {
                handler.execute(taskInstance, executionContext);
            }
        };
    }

}
