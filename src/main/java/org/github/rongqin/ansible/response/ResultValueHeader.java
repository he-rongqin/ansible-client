package org.github.rongqin.ansible.response;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.Data;
import org.github.rongqin.ansible.response.ReturnValue.Result;

@Data
public class ResultValueHeader {

	public static Pattern head_type1_pattern = Pattern.compile("^\\d+\\.\\d+\\.\\d+\\.\\d+( \\| ).*( \\| ).*>>$");
	public static Pattern head_type1_rc_pattern = Pattern.compile("rc=\\d");
	public static Pattern head_type1_result_pattern = Pattern.compile("( \\| ).*( \\| )");
	public static Pattern head_type2_pattern = Pattern.compile("^\\d+\\.\\d+\\.\\d+\\.\\d+( \\| ).+=>.*\\{$");
	public static Pattern head_type2_result_pattern = Pattern.compile("( \\| ).*( =>)");
	public static Pattern head_inventory_no_host_pattern = Pattern.compile(".*(Could not match supplied host pattern).*");
	public static Pattern head_ip_pattern = Pattern.compile("\\d+\\.\\d+\\.\\d+\\.\\d+");
	private String ip;
	private Result result;
	private Integer rc = -1;

	public ResultValueHeader() {
	}




	public static ResultValueHeader createHeader(String headerLine) {
		ResultValueHeader header = new ResultValueHeader();
		ResultValueHeader.HeaderType headerType;
		if (head_type1_pattern.matcher(headerLine).matches()) {
			headerType = ResultValueHeader.HeaderType.type1;
		} else if (head_type2_pattern.matcher(headerLine).matches()) {
			headerType = ResultValueHeader.HeaderType.type2;
		} else {
			if (!head_inventory_no_host_pattern.matcher(headerLine).matches()) {
				return null;
			}

			headerType = ResultValueHeader.HeaderType.type_not_in_inventory;
		}

		header.parseHeader(headerType, headerLine);
		return header;
	}

	private void parseHeader(ResultValueHeader.HeaderType headerType, String headerLine) {
		Matcher matchIp = head_ip_pattern.matcher(headerLine);
		if (matchIp.find()) {
			this.ip = matchIp.group();
		}

		Matcher matchResult;
		String rawResult;
		switch(headerType) {
			case type1:
				matchResult = head_type1_rc_pattern.matcher(headerLine);
				if (matchResult.find()) {
					rawResult = matchResult.group();
					int code = Integer.valueOf(rawResult.replaceAll("rc=", ""));
					this.rc = code;
				}

				matchResult = head_type1_result_pattern.matcher(headerLine);
				if (matchResult.find()) {
					rawResult = matchResult.group();
					this.result = this.transferResult(rawResult.replaceAll("\\|", "").replaceAll(" ", ""));
				}
				break;
			case type2:
				matchResult = head_type2_result_pattern.matcher(headerLine);
				if (matchResult.find()) {
					rawResult = matchResult.group();
					this.result = this.transferResult(rawResult.replaceAll("\\|", "").replaceAll(" ", "").replaceAll("=>", ""));
				}
				break;
			case type_not_in_inventory:
				this.result = Result.unmanaged;
		}

	}

	private Result transferResult(String result) {
		if ("CHANGED".equals(result)) {
			return Result.changed;
		} else if ("SUCCESS".equals(result)) {
			return Result.success;
		} else if ("UNREACHABLE!".equals(result)) {
			return Result.unreachable;
		} else {
			return "FAILED!".equals(result) ? Result.failed : Result.unknown;
		}
	}

	@Override
	public String toString() {
		return this.ip + " " + this.rc + " " + this.result;
	}

	static enum HeaderType {
		type1,
		type2,
		type_not_in_inventory;

		private HeaderType() {
		}
	}
}
