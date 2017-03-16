package spc.webos.service.seq.impl;

import java.util.Date;

import org.apache.commons.lang3.time.FastDateFormat;

import spc.webos.service.seq.UUID;

public class TimeMillisUUID implements UUID
{
	public TimeMillisUUID()
	{
	}

	public TimeMillisUUID(int workerId)
	{
		this.workerId = workerId;
		if (workerId > MAX_WORKER_ID)
			throw new RuntimeException("workerId:" + workerId + " >" + MAX_WORKER_ID + " !!!");
	}

	public synchronized long uuid()
	{
		long millis = System.currentTimeMillis();
		// 如果大于上一毫秒则清0
		if (millis > lastMillis) return uuid(lastMillis = millis, seq = 0);

		if (seq < MAX_SEQ_ID) return uuid(lastMillis, ++seq); // 如果相同则增加自增号
		// 自增位已经达到9，需要借用下一个毫秒， 并把seq清0
		return uuid(++lastMillis, seq = 0);
	}

	public String format(long uuid)
	{
		String dt = FastDateFormat.getInstance(format).format(new Date(uuid / 100000));
		String str = String.valueOf(uuid);
		return dt + str.substring(str.length() - 5);
	}

	// workId + 时间戳 + 自增号
	protected long uuid(long millis, int seq)
	{
		return millis * 100000 + seq * 1000 + workerId; // 如果相同则增加自增号
	}

	public final static int MAX_SEQ_ID = 99; // 最大seq id, 考虑到long的大小最大为19位
	public final static int MAX_WORKER_ID = 999; // 最大worker id
	protected int workerId;
	private int seq; // 同一毫秒下的自增, synchronized模式下cpu性能一毫秒申请不会超过100，一般20多
	private long lastMillis = 0; // 作为整个虚拟机流水号生成的基础，每次生成一个唯一的时间，如果一毫秒有两笔请求则借用未来一毫秒
	protected String format = "yyMMddHHmmssSSS"; // 0:毫秒模式，1:yyMMddHHmmssSSS

	public void setWorkerId(int workerId)
	{
		if (workerId > MAX_WORKER_ID)
			throw new RuntimeException("workerId:" + workerId + " >" + MAX_WORKER_ID + " !!!");
		this.workerId = workerId;
	}

	public void setFormat(String format)
	{
		this.format = format;
	}
}
