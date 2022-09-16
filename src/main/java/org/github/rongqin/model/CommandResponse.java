package org.github.rongqin.model;


import lombok.*;

/**
 * @author herongqin
 */
@Data
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CommandResponse {

	public int state;

	private String info;

	private String error;

	private boolean success;

	public boolean isSuccess(){
		return state == 0;
	}

}
