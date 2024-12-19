package org.apache.logging.log4j;

import net.sourceforge.squirrel_sql.fw.util.log.ILogger;
import net.sourceforge.squirrel_sql.fw.util.log.LoggerController;
import org.apache.logging.log4j.message.Message;

import java.util.List;

public class FixPoiLog4jDependencyLogBuilderMock implements LogBuilder
{
   public final static ILogger s_log = LoggerController.createLogger(FixPoiLog4jDependencyLogBuilderMock.class);
   private final Level _level;

   public FixPoiLog4jDependencyLogBuilderMock(Level level)
   {
      _level = level;
   }

   @Override
   public LogBuilder withThrowable(Throwable throwable)
   {
      return this;
   }

   @Override
   public LogBuilder withLocation()
   {
      return this;
   }

   @Override
   public void log(CharSequence message)
   {
      if(noLog()){return;}

      s_log.info(message);
   }

   @Override
   public void log(String message)
   {
      logMultParam(message);
   }

   @Override
   public void log(String message, Object... params)
   {
      logMultParam(message, params);
   }

   private void logMultParam(String message, Object... params)
   {
      if(noLog()){return;}

      s_log.info(message + " | PARAMS: " + List.of(params));
   }

   @Override
   public void log(String message, org.apache.logging.log4j.util.Supplier<?>... params)
   {
      if(noLog()){return;}

      s_log.info(message + " | PARAMS: " + params);
   }

   @Override
   public void log(Message message)
   {
      if(noLog()){return;}

      s_log.info(message);
   }

   @Override
   public void log(org.apache.logging.log4j.util.Supplier<Message> messageSupplier)
   {
      if(noLog()){return;}

      s_log.info(messageSupplier.get());
   }

   @Override
   public Message logAndGet(org.apache.logging.log4j.util.Supplier<Message> messageSupplier)
   {
      if(noLog()){return messageSupplier.get();}

      s_log.info(messageSupplier.get());
      return messageSupplier.get();
   }

   @Override
   public void log(Object message)
   {
      if(noLog()){return;}
      s_log.info(message);
   }

   @Override
   public void log(String message, Object p0)
   {
      logMultParam(message, p0);
   }

   @Override
   public void log(String message, Object p0, Object p1)
   {
      logMultParam(message, p0, p1);
   }

   @Override
   public void log(String message, Object p0, Object p1, Object p2)
   {
      logMultParam(message, p0, p1, p2);
   }

   @Override
   public void log(String message, Object p0, Object p1, Object p2, Object p3)
   {
      logMultParam(message, p0, p1, p2, p3);
   }

   @Override
   public void log(String message, Object p0, Object p1, Object p2, Object p3, Object p4)
   {
      logMultParam(message, p0, p1, p2, p3, p4);
   }

   @Override
   public void log(String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5)
   {
      logMultParam(message, p0, p1, p2, p3, p5);
   }

   @Override
   public void log(String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6)
   {
      logMultParam(message, p0, p1, p2, p3, p5, p6);
   }

   @Override
   public void log(String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6, Object p7)
   {
      logMultParam(message, p0, p1, p2, p3, p5, p6, p7);
   }

   @Override
   public void log(String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6, Object p7, Object p8)
   {
      logMultParam(message, p0, p1, p2, p3, p5, p6, p7, p8);
   }

   @Override
   public void log(String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6, Object p7, Object p8, Object p9)
   {
      logMultParam(message, p0, p1, p2, p3, p5, p6, p7, p8, p9);
   }

   @Override
   public void log()
   {
      logMultParam("EMPTY");
   }

   private boolean noLog()
   {
      return _level == Level.TRACE || _level == Level.DEBUG || _level == Level.INFO;
   }
}
