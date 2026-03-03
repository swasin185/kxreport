package com.keehin.kxreport;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ParameterTest {

	@Test
	void setters_convert_blank_strings_to_null() {
		Parameter p = new Parameter();
		p.setApp(" ");
		p.setDb("");
		p.setReport("\t");
		p.setComCode(null);

		assertThat(p.getApp()).isNull();
		assertThat(p.getDb()).isNull();
		assertThat(p.getReport()).isNull();
		assertThat(p.getComCode()).isNull();
	}

	@Test
	void setters_keep_non_blank_values() {
		Parameter p = new Parameter();
		p.setApp("app");
		p.setDb("db1");
		p.setReport("A00");

		assertThat(p.getApp()).isEqualTo("app");
		assertThat(p.getDb()).isEqualTo("db1");
		assertThat(p.getReport()).isEqualTo("A00");
	}
}
