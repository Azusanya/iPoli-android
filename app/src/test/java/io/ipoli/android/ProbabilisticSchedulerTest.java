package io.ipoli.android;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import io.ipoli.android.app.scheduling.DiscreteDistribution;
import io.ipoli.android.app.scheduling.ProbabilisticTaskScheduler;
import io.ipoli.android.app.scheduling.Task;
import io.ipoli.android.app.scheduling.TimeBlock;
import io.ipoli.android.app.utils.Time;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 12/16/16.
 */

public class ProbabilisticSchedulerTest {

    @Test
    public void shouldGiveAllAvailableSlots() {
        List<Task> tasks = new ArrayList<>();
        tasks.add(new Task(10, 20));
        Random random = new Random(42);
        ProbabilisticTaskScheduler scheduler = new ProbabilisticTaskScheduler(0, 1, tasks, random);

        double[] values = new double[61];
        for (int i = 0; i < values.length; i++) {
            values[i] = random.nextInt(100);
        }
        DiscreteDistribution dist = new DiscreteDistribution(values, random);

        List<TimeBlock> slots = scheduler.chooseSlotsFor(new Task(10), 15, Time.now(), dist);
        assertThat(slots.size(), is(3));
    }

    @Test
    public void shouldHaveHighProbSlotAsFirstChoice() {
        List<Task> tasks = new ArrayList<>();
        tasks.add(new Task(10, 20));
        Random random = new Random(42);
        ProbabilisticTaskScheduler scheduler = new ProbabilisticTaskScheduler(0, 1, tasks, random);

        double[] values = new double[60];
        for (int i = 0; i < values.length; i++) {
            values[i] = 1;
        }
        DiscreteDistribution dist = new DiscreteDistribution(values, random);

        List<TimeBlock> slots = scheduler.chooseSlotsFor(new Task(10), 15, Time.now(), dist);
        assertThat(slots.get(0).getProbability(), greaterThan(0.183));
    }
}
