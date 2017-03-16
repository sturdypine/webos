package spc.webos.service.seq.impl;

import spc.webos.service.BaseService;
import spc.webos.service.seq.UUID;

public class SnowflakeUUID extends BaseService implements UUID
{
	private long workerId;

	private final static long twepoch = 1361753741828L;
	private static long sequence = 0L;
	private final static long workerIdBits = 4L;
	public final static long maxWorkerId = -1L ^ -1L << workerIdBits;
	private final static long sequenceBits = 10L;

	private final static long workerIdShift = sequenceBits;
	private final static long timestampLeftShift = sequenceBits + workerIdBits;
	public final static long sequenceMask = -1L ^ -1L << sequenceBits;
	private static long lastTimestamp = -1L;
	static SnowflakeUUID uuid = new SnowflakeUUID();

	private SnowflakeUUID()
	{
	}

	public static SnowflakeUUID getInstance()
	{
		return uuid;
	}

	public void setWorkerId(long id)
	{
		if (workerId > maxWorkerId || workerId < 0) { throw new IllegalArgumentException(
				String.format("worker Id can't be greater than %d or less than 0", maxWorkerId)); }
		workerId = id;
	}

	public String format(long uuid)
	{
		return String.valueOf(uuid);
	}

	public synchronized long uuid()
	{
		long timestamp = System.currentTimeMillis();
		if (lastTimestamp == timestamp)
		{
			sequence = (sequence + 1) & sequenceMask;
			if (sequence == 0)
			{
				// System.out.println("###########" + sequenceMask);
				timestamp = tilNextMillis(lastTimestamp);
			}
		}
		else sequence = 0;
		if (timestamp < lastTimestamp) throw new RuntimeException(
				String.format("Clock moved backwards.  Refusing to generate id for %d milliseconds",
						lastTimestamp - timestamp));

		lastTimestamp = timestamp;
		long nextId = ((timestamp - twepoch << timestampLeftShift)) | (workerId << workerIdShift)
				| (sequence);
		return nextId;
	}

	private static long tilNextMillis(final long lastTimestamp)
	{
		long timestamp = System.currentTimeMillis();
		while (timestamp <= lastTimestamp)
		{
			timestamp = System.currentTimeMillis();
		}
		return timestamp;
	}

	public static void main(String[] args) throws Exception
	{
		SnowflakeUUID uuid = SnowflakeUUID.getInstance();
		uuid.setWorkerId(2);
		System.out.println(uuid.uuid());
		System.out.println(uuid.uuid());
		System.out.println(uuid.uuid());
		System.out.println(uuid.uuid());
		System.out.println(uuid.uuid());
	}
}
