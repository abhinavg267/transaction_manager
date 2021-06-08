package modules

import com.google.inject.AbstractModule
import utils.DataBase

class BindModule extends AbstractModule {
  override def configure(): Unit = {
    DataBase
  }
}
