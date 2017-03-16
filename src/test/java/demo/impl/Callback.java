package demo.impl;

import demo.ICallback;

public class Callback implements ICallback, java.io.Serializable {
	int no;

	public Callback(int no) {
		this.no = no;
	}

	public String callback(String name) {
		String msg = "consumer callback:" + name + " by no:" + no;
		System.out.println(msg);
		return msg;
	}

}
