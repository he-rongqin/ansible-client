package org.github.rongqin.ansible.response;

import cn.hutool.json.JSONObject;
import lombok.Data;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author herongqin
 */
@Data
@ToString
public class ReturnValue {
	private ReturnValue.Result result;
	private int rc;
	private JSONObject value;
	private List<String> stdout = new ArrayList();

	public static enum Result {
		failed,
		changed,
		success,
		unreachable,
		unmanaged,
		unknown;

		private Result() {
		}
	}

}
