package com.emumaelish.dto;

import io.swagger.annotations.ApiModelProperty;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotEmpty;

/**
 * @author: chenyu
 * @Date: 2019/8/24 17:18
 * @Description:
 */
public class PutRequest {

	@ApiModelProperty(value = "上级设备编号",position = 1,required = true)
	@NotEmpty(message = "groupId不能为空")
	@Length(min=1,max=32,message = "groupId长度不能超过32")
	private String groupId;

	@ApiModelProperty(value = "key",position = 1,required = true)
	@NotEmpty(message = "key不能为空")
	private String key;

	@ApiModelProperty(value = "值",position = 1,required = true)
	@NotEmpty(message = "value不能为空")
	private String value;

	public String getGroupId() {
		return groupId;
	}

	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
}
