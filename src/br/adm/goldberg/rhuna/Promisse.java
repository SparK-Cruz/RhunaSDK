package br.admin.goldberg.rhuna;

import java.util.ArrayList;

public class Promisse{
  public enum Status{
    READY,
    FULLFILLED,
    FAILED
  }

  private Status status = Status.READY;
  private int tasksFinished = 0;

  private List<Task> tasks;

  private ArrayList<Thread> threads = new ArrayList<Thread>();
  private ArrayList<Object> results = new ArrayList<Object>();
  private ArrayList<Callback> listeners = new ArrayList<Callback>();

  public Promisse(Task... tasks){
    this.tasks = Arrays.asList(tasks);

    int index = 0;
    for(Task task : tasks){
      threads.add(setupTask(index, task));
      index++;
    }

    triggerThreads();
  }

  public void then(Callback listener){
    listeners.add(listener);
  }

  private Thread setupTask(final int i, final Task task){
    return new Thread(new Runnable(){
      @Override
      public void run(){
        try{
          results.add(i, task.run());
          onTaskDone(i);
        }catch(Exception e){
          onTaskFailed(e);
        }
      }
    });
  }
  private void triggerThreads(){
    for(Thread thread : threads){
      thread.start();
    }
  }

  private void onTaskDone(int index){
    this.tasksFinished++;
    if(tasksFinished != tasks.size())
      return;

    for(Callback listener : listeners){
      listener.done(results);
    }
  }

  private void onTaskFailed(Exception e){
    if(this.status == Status.FAILED)
      return;

    this.status = Status.FAILED;

    for(Thread thread : threads){
      thread.interrupt();
    }

    for(Callback listener : listeners){
      listener.fail(e);
    }
  }

  public interface Task{
    Object run() throws Exception;
  }

  public interface Callback{
    void done(Object... results);
    void fail(Exception e);
  }
}
