/**
# Copyright Rakesh Shrestha (rakesh.shrestha@gmail.com)
# All rights reserved.
#
# Redistribution and use in source and binary forms, with or without
# modification, are permitted provided that the following conditions are
# met:
#
# Redistributions must retain the above copyright notice.
 */

package io.javalin.util

import com.mashape.unirest.http.HttpResponse
import com.mashape.unirest.http.ObjectMapper
import com.mashape.unirest.http.Unirest
import io.javalin.core.util.Header
import io.javalin.plugin.json.JavalinJson

import app.domain.User
import app.domain.UserDTO

class HttpUtil() {
	private val json = "application/json"
	val headers = mutableMapOf(Header.ACCEPT to json, Header.CONTENT_TYPE to json)

	init {
		Unirest.setObjectMapper(
			object : ObjectMapper {
				override fun <T> readValue(value: String, valueType: Class<T>): T {
					return JavalinJson.fromJson(value, valueType)
				}

				override fun writeValue(value: Any): String {
					return JavalinJson.toJson(value)
				}
			}
		)
	}

	@JvmField
	val origin: String = "http://localhost:8080"

	inline fun <reified T> post(path: String) =
			Unirest.post(origin + path).headers(headers).asObject(T::class.java)

	inline fun <reified T> post(path: String, body: Any) =
			Unirest.post(origin + path).headers(headers).body(body).asObject(T::class.java)

	inline fun <reified T> get(path: String, params: Map<String, Any>? = null) =
			Unirest.get(origin + path).headers(headers).queryString(params).asObject(T::class.java)

	inline fun <reified T> put(path: String, body: Any) =
			Unirest.put(origin + path).headers(headers).body(body).asObject(T::class.java)

	inline fun <reified T> deleteWithResponseBody(path: String) =
			Unirest.delete(origin + path).headers(headers).asObject(T::class.java)

	fun delete(path: String) =
			Unirest.delete(origin + path).headers(headers).asString()

	fun loginAndSetTokenHeader(email: String, password: String) {
		val userDTO = UserDTO(User(email = email, password = password))
		val response = post<UserDTO>("/api/users/login", userDTO)
		headers["Authorization"] = "Token ${response.body.user?.token}"
	}

	fun registerUser(email: String, password: String, username: String): UserDTO {
		val userDTO = UserDTO(User(email = email, password = password, username = username))
		val response = post<UserDTO>("/api/users", userDTO)
		return response.body
	}

	fun createUser(userEmail: String = "user@valid_user_mail.com", username: String = "user_name_test"): UserDTO {
		val password = "password"
		val user = registerUser(userEmail, password, username)
		loginAndSetTokenHeader(userEmail, password)
		return user
	}

}