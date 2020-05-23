package com.fet.lineBot.domain.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;

@Entity
@Table(name = "MEMBER")
@Data
public class MemberData {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;
	@Column(name = "USER_ID")
	private String userId;
	@Column(name = "LINE_NAME")
	private String lineName;
	@Column(name = "EXPIRATION_DATE")
	private Date expirationDate;
	
}
