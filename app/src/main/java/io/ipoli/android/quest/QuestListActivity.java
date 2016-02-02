package io.ipoli.android.quest;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.StringRes;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import java.util.List;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import io.ipoli.android.Constants;
import io.ipoli.android.R;
import io.ipoli.android.app.BaseActivity;
import io.ipoli.android.app.utils.DateUtils;
import io.ipoli.android.quest.events.CompleteQuestEvent;
import io.ipoli.android.quest.events.CompleteQuestRequestEvent;
import io.ipoli.android.quest.events.EditQuestRequestEvent;
import io.ipoli.android.quest.events.QuestUpdatedEvent;
import io.ipoli.android.quest.events.UndoCompleteQuestEvent;
import io.ipoli.android.quest.persistence.QuestPersistenceService;

public class QuestListActivity extends BaseActivity {

    @Bind(R.id.quest_list_container)
    LinearLayout rootContainer;

    @Bind(R.id.toolbar)
    Toolbar toolbar;

    @Inject
    Bus eventBus;

    @Bind(R.id.quest_list)
    RecyclerView questList;

    @Inject
    QuestPersistenceService questPersistenceService;

    private QuestAdapter questAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quest_list);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        appComponent().inject(this);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        questList.setLayoutManager(layoutManager);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();
        eventBus.register(this);
        List<Quest> quests = questPersistenceService.findAllPlannedForToday();
        questAdapter = new QuestAdapter(this, quests, eventBus);
        questList.setAdapter(questAdapter);
    }

    @Override
    public void onPause() {
        eventBus.unregister(this);
        super.onPause();
    }

    @Subscribe
    public void onQuestCompleteRequest(final CompleteQuestRequestEvent e) {
        Intent i = new Intent(this, QuestCompleteActivity.class);
        i.putExtra(Constants.QUEST_ID_EXTRA_KEY, e.quest.getId());
        i.putExtra(Constants.POSITION_EXTRA_KEY, e.position);
        startActivityForResult(i, Constants.COMPLETE_QUEST_RESULT_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if ((resultCode == RESULT_OK || resultCode == RESULT_CANCELED) && requestCode == Constants.COMPLETE_QUEST_RESULT_REQUEST_CODE) {
            final int position = data.getIntExtra(Constants.POSITION_EXTRA_KEY, -1);
            final String id = data.getStringExtra(Constants.QUEST_ID_EXTRA_KEY);
            final Quest q = questPersistenceService.findById(id);
            final Difficulty d = (Difficulty) data.getSerializableExtra(QuestCompleteActivity.DIFFICULTY_EXTRA_KEY);
            final String log = data.getStringExtra(QuestCompleteActivity.LOG_EXTRA_KEY);

            final Snackbar snackbar = Snackbar
                    .make(rootContainer,
                            getString(R.string.quest_complete),
                            Snackbar.LENGTH_SHORT);

            snackbar.setCallback(new Snackbar.Callback() {
                @Override
                public void onDismissed(Snackbar snackbar, int event) {
                    super.onDismissed(snackbar, event);
                    q.setStatus(Status.COMPLETED.name());
                    q.setLog(log);
                    q.setDifficulty(d.name());
                    questPersistenceService.save(q);
                    eventBus.post(new CompleteQuestEvent(q));
                }

            });

            snackbar.setAction(R.string.undo, new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (position >= 0) {
                        questAdapter.putQuest(position, q);
                    } else {
                        questAdapter.addQuest(q);
                    }
                    snackbar.setCallback(null);
                    eventBus.post(new UndoCompleteQuestEvent(q));
                }
            });

            snackbar.show();
        } else if (resultCode == RESULT_OK && requestCode == Constants.EDIT_QUEST_RESULT_REQUEST_CODE) {
            final int position = data.getIntExtra(Constants.POSITION_EXTRA_KEY, -1);
            final String id = data.getStringExtra(Constants.QUEST_ID_EXTRA_KEY);
            final Quest q = questPersistenceService.findById(id);
            if (DateUtils.isToday(q.getDue())) {
                List<Quest> quests = questPersistenceService.findAllPlannedForToday();
                questAdapter = new QuestAdapter(this, quests, eventBus);
                questList.setAdapter(questAdapter);
            } else {
                questAdapter.removeQuest(position);
            }
        }
    }

    @Subscribe
    public void onQuestUpdated(QuestUpdatedEvent e) {
        questPersistenceService.save(e.quest);
    }

    private void showSnackBar(@StringRes int textRes) {
        Snackbar.make(rootContainer, getString(textRes), Snackbar.LENGTH_SHORT).show();
    }

    @Subscribe
    public void onEditQuestRequest(EditQuestRequestEvent e) {
        Intent i = new Intent(this, EditQuestActivity.class);
        i.putExtra(Constants.QUEST_ID_EXTRA_KEY, e.questId);
        i.putExtra(Constants.POSITION_EXTRA_KEY, e.position);
        startActivityForResult(i, Constants.EDIT_QUEST_RESULT_REQUEST_CODE);
    }
}
