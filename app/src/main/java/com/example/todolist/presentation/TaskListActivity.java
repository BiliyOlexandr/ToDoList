package com.example.todolist.presentation;

import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.Toast;

import com.example.todolist.ChangeTaskActivity;
import com.example.todolist.R;
import com.example.todolist.model.ToDoListDataBaseHelper;
import com.example.todolist.model.Task;
import com.example.todolist.model.TaskDB;

import java.util.ArrayList;
import java.util.List;

public class TaskListActivity extends AppCompatActivity   {
  // Position the task in list, add new pc to same repository (To do list)
  public static final String CHANGE_EXTRA_TASKNO = "changetaskNo";

  private SQLiteOpenHelper todolistDatabaseHelper;
  private SQLiteDatabase db;
  private Cursor cursor;
  private TaskAdapter myAdapter;
  private List<Task> taskList;
  private List<TaskDB> mTaskDB;
  private Intent intent;
  private  RecyclerView recyclerView;
  private TaskListViewModel mModel;

  public TaskListActivity() {
  }

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.new_activity_main);

    mModel = ViewModelProviders.of(this).get(TaskListViewModel.class);

    mModel.getTasks().observe(this, users ->{
      myAdapter.clear();
      myAdapter.addAll(users);
      myAdapter.notifyDataSetChanged();
      // update UI
    });

    RecyclerView recyclerView = (RecyclerView) findViewById(R.id.my_recycler_view);
    taskList = new ArrayList<>();

    todolistDatabaseHelper = new ToDoListDataBaseHelper(this);

    recyclerView.setLayoutManager(new LinearLayoutManager(this));

    myAdapter = new TaskAdapter(mTaskDB, username -> {

      intent = new Intent(this, ChangeTaskActivity.class);
      intent.putExtra(CHANGE_EXTRA_TASKNO, username);
      startActivity(intent);
    }, username -> new AlertDialog.Builder(this).setTitle(R.string.delete)
        .setMessage(R.string.was_deleted)
        .setPositiveButton(R.string.ok, (dialogInterface, i) -> {
          Toast.makeText(TaskListActivity.this, R.string.was_deleted, Toast.LENGTH_LONG).show();
          //Code to delete DB columns
          db = todolistDatabaseHelper.getWritableDatabase();
          db.delete(ToDoListDataBaseHelper.TABLE_NAME, "NAME = ?", new String[] { username });
          db.close();
          getTasks();
        })
        .setNegativeButton(R.string.cencel, null)
        .show());

    recyclerView.setAdapter(myAdapter);

    findViewById(R.id.new_fab).setOnClickListener(view -> {
      Intent intent = new Intent(TaskListActivity.this, OneTaskActivity.class);
      startActivity(intent);
    });
  }

  private void getTasks() {
    taskList.clear();
    db = todolistDatabaseHelper.getReadableDatabase();
    cursor = db.query(ToDoListDataBaseHelper.TABLE_NAME,
        new String[] { ToDoListDataBaseHelper.NAME, ToDoListDataBaseHelper.DISCRIPTION }, null,
        null, null, null, null);

    for (int i = 0; i <= cursor.getCount(); i++) {
      if (cursor.moveToNext()) {
        Task task = new Task(cursor.getString(0), cursor.getString(1));
        taskList.add(task);
      }
    }
    if (myAdapter != null) {
      myAdapter.notifyDataSetChanged();
    }

  }

  @Override protected void onStart() {
    super.onStart();
    getTasks();
  }

  @Override protected void onDestroy() {
    super.onDestroy();
    cursor.close();
    db.close();
  }

  interface OnTaskClickListener {
    void onTaskClicked(String username);
  }

  interface OnLongTaskClickListener {
    void onLongTaskClicked(String username);
  }
}

