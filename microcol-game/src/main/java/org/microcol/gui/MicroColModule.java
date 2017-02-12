package org.microcol.gui;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;

public class MicroColModule extends AbstractModule {

  @Override
  protected void configure() {

    bind(StatusBarMessageController.class).to(StatusBarMessageControllerImpl.class)
        .in(Singleton.class);
    
    
    bind(StatusBarView.class).in(Singleton.class);
    bind(StatusBarPresenter.Display.class).to(StatusBarView.class).in(Singleton.class);
    bind(StatusBarPresenter.class).asEagerSingleton();
    
    bind(MainMenuView.class).in(Singleton.class);
    bind(MainMenuPresenter.Display.class).to(MainMenuView.class).in(Singleton.class);
    bind(MainMenuPresenter.class).asEagerSingleton();
    
  }

}
