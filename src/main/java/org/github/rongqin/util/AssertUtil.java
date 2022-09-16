package org.github.rongqin.util;

/**
 * 断言工具
 * @author herongqin
 */
public class AssertUtil {

	public static void isTrue(Boolean value, String message){
		if (!value){
			throw new RuntimeException(message);
		}
	}
}
