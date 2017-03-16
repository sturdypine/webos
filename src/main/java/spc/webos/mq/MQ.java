package spc.webos.mq;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.core.JmsOperations;
import org.springframework.jms.core.MessageCreator;

import com.google.gson.reflect.TypeToken;

import spc.webos.constant.AppRetCode;
import spc.webos.exception.AppException;
import spc.webos.exception.Status;

public interface MQ
{
	public final static List<String> REQUST_QUEUE = new ArrayList<>();
	public final static List<String> RESPONSE_QUEUE = new ArrayList<>();
	public final static Map<String, String> QUEUES = new HashMap<>();

	JmsOperations jms();

	void execute(Consumer<JmsOperations> consumer) throws Exception;

	void callback(Map<String, Object> soap);

	void notice(String queue, String method, String args) throws AppException;

	void noticeMap(String queue, String method, Map<String, Object> args) throws AppException;

	void noticeList(String queue, String method, List<Object> args) throws AppException;

	void snd(String queue, String method, String resQ, String callback, String args)
			throws AppException;

	void sndMap(String queue, String method, String resQ, String callback, Map<String, Object> args)
			throws AppException;

	void sndList(String queue, String method, String resQ, String callback, List<Object> args)
			throws AppException;

	void send(String queue, String sndAppCd, String sn, String method, String resQ, String callback,
			Object args, int expire, MessageCreator messageCreator);

	<T> T sync(String queue, String method, String args, Future<T> future) throws AppException;

	<T> T sync(String queue, String method, List<Object> args, Future<T> future)
			throws AppException;

	<T> T sync(String queue, String method, Map<String, Object> args, Future<T> future)
			throws AppException;

	<T> void async(String queue, String method, String args, Callback<T> callback)
			throws AppException;

	<T> void async(String queue, String method, List<Object> args, Callback<T> callback)
			throws AppException;

	<T> void async(String queue, String method, Map<String, Object> args, Callback<T> callback)
			throws AppException;

	<T> void asyncall(String queue, String sndAppCd, String sn, String method, Object args,
			int timeout, int expire, MessageCreator messageCreator, Callback<T> callback)
			throws AppException;

	public class Future<T> extends Callback<T>
	{
		protected T ret;
		protected Map<String, String> status;
		protected int timeout;

		public Future(int timeout)
		{
			this.timeout = timeout;
		}

		public synchronized void callback(T r, Map<String, String> s)
		{
			ret = r;
			status = s;
			notifyAll();
		}

		public synchronized T get()
		{
			long start = System.currentTimeMillis();
			try
			{
				while (status == null && System.currentTimeMillis() - start < timeout)
					wait(timeout);
			}
			catch (InterruptedException e)
			{
				throw new AppException(AppRetCode.CMMN_BUF_TIMEOUT);
			}
			if (ret != null) return ret;
			if (status == null) throw new AppException(AppRetCode.CMMN_BUF_TIMEOUT);
			String retCd = status.get(Status.RETCD);
			if (!Status.isSuccess(retCd)) throw new AppException(retCd, status.get(Status.DESC));
			return ret;
		}

		public int getTimeout()
		{
			return timeout;
		}
	}

	public abstract class Callback<T> extends TypeToken<T> implements AutoCloseable
	{
		protected final Logger log = LoggerFactory.getLogger(getClass());

		public abstract void callback(T r, Map<String, String> s);

		public void close()
		{
		}
	}
}
