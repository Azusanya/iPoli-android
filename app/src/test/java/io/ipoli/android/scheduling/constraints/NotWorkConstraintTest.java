package io.ipoli.android.scheduling.constraints;

import org.junit.Before;
import org.junit.Test;
import org.threeten.bp.DayOfWeek;
import org.threeten.bp.LocalDate;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import io.ipoli.android.app.scheduling.DailyScheduler;
import io.ipoli.android.app.scheduling.Task;
import io.ipoli.android.app.scheduling.constraints.Constraint;
import io.ipoli.android.app.scheduling.constraints.NotWorkConstraint;
import io.ipoli.android.app.scheduling.distributions.DiscreteDistribution;
import io.ipoli.android.app.utils.TimePreference;
import io.ipoli.android.quest.data.Category;
import io.ipoli.android.quest.data.Quest;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 4/22/17.
 */
public class NotWorkConstraintTest {

    private Constraint constraint;

    @Before
    public void setUp() throws Exception {
        Set<DayOfWeek> workDays = new HashSet<>(Collections.singleton(LocalDate.now().getDayOfWeek()));
        constraint = new NotWorkConstraint(0, 30, workDays, DailyScheduler.DEFAULT_TIME_SLOT_DURATION);
    }

    @Test
    public void shouldApplyToNonWorkTask() {
        Task t = new Task(30, Quest.PRIORITY_NOT_IMPORTANT_URGENT, TimePreference.PERSONAL_HOURS, Category.WELLNESS);
        assertTrue(constraint.shouldApply(t));
    }

    @Test
    public void shouldNotApplyToWorkTask() {
        Task t = new Task(30, Quest.PRIORITY_NOT_IMPORTANT_URGENT, TimePreference.PERSONAL_HOURS, Category.WORK);
        assertFalse(constraint.shouldApply(t));
    }

    @Test
    public void shouldHaveNonZeroProbabilityOutsideWorkTime() {
        DiscreteDistribution dist = constraint.apply();
        assertThat(dist.at(constraint.getSlotForMinute(55)), is(greaterThan(0.0)));
    }

    @Test
    public void shouldHaveZeroProbabilityWithinWorkTime() {
        DiscreteDistribution dist = constraint.apply();
        assertThat(dist.at(constraint.getSlotForMinute(0)), is(0.0));
    }

    @Test
    public void shouldHaveNonZeroProbabilityAtStartOfNonWorkTime() {
        DiscreteDistribution dist = constraint.apply();
        assertThat(dist.at(constraint.getSlotForMinute(30)), is(greaterThan(0.0)));
    }
}
