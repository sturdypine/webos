package demo;

public interface INotify {
	public void onreturn(Object msg, Object... name);

	public void onthrow(Throwable ex, Object... name);
}
