package chocoteamteam.togather.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import chocoteamteam.togather.config.SecurityConfig;
import chocoteamteam.togather.dto.ChatDetailDto;
import chocoteamteam.togather.dto.ChatMessageDto;
import chocoteamteam.togather.dto.ChatRoomDto;
import chocoteamteam.togather.dto.CreateChatRoomForm;
import chocoteamteam.togather.service.JwtService;
import chocoteamteam.togather.service.ProjectChatRoomService;
import chocoteamteam.togather.testUtils.WithLoginMember;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(value = ProjectChatRoomController.class,
	includeFilters = {
		@ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = SecurityConfig.class)})
class ProjectChatRoomControllerTest {

	@Autowired
	MockMvc mockMvc;
	@Autowired
	ObjectMapper objectMapper;

	@MockBean
	JwtService jwtService;

	@MockBean
	ProjectChatRoomService projectChatRoomService;

	@WithLoginMember
	@DisplayName("채팅방 생성 API 성공")
	@Test
	void createProjectChat_success() throws Exception {
		//given
		CreateChatRoomForm form = CreateChatRoomForm.builder()
			.roomName("test").build();

		ChatRoomDto dto = ChatRoomDto.builder()
			.roomId(1L)
			.roomName("test")
			.build();

		given(projectChatRoomService.createChatRoom(any()))
			.willReturn(dto);

		//when
		//then
		mockMvc.perform(post("/projects/1/chats")
				.content(objectMapper.writeValueAsString(form))
				.contentType(MediaType.APPLICATION_JSON))
			.andDo(print())
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.roomId").value(dto.getRoomId()))
			.andExpect(jsonPath("$.roomName").value(dto.getRoomName()));
	}

	@WithLoginMember
	@DisplayName("채팅방 리스트 조회 API 성공")
	@Test
	void getProjectChats_success() throws Exception {
		//given
		ChatRoomDto dto = ChatRoomDto.builder()
			.roomId(1L)
			.roomName("test")
			.build();

		List<ChatRoomDto> chatRoomDtos = Arrays.asList(dto);

		given(projectChatRoomService.getChatRooms(anyLong(), anyLong()))
			.willReturn(chatRoomDtos);

		ArgumentCaptor<Long> memberIdCaptor = ArgumentCaptor.forClass(Long.class);
		ArgumentCaptor<Long> projectIdCaptor = ArgumentCaptor.forClass(Long.class);

		//when
		//then
		mockMvc.perform(get("/projects/1/chats"))
			.andDo(print())
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.chatRoomDtos[0].roomId").value(dto.getRoomId()))
			.andExpect(jsonPath("$.chatRoomDtos[0].roomName").value(dto.getRoomName()));

		verify(projectChatRoomService)
			.getChatRooms(projectIdCaptor.capture(), memberIdCaptor.capture());

		assertThat(projectIdCaptor.getValue()).isEqualTo(1L);
		assertThat(memberIdCaptor.getValue()).isEqualTo(1L);
	}

	@WithLoginMember
	@DisplayName("채팅방 상세 조회 API 성공")
	@Test
	void getProjectChat_success() throws Exception {
		//given
		ChatMessageDto message = ChatMessageDto.builder()
			.message("test")
			.nickname("tester")
			.build();

		List<ChatMessageDto> messages = Arrays.asList(message);

		ChatDetailDto dto = ChatDetailDto.builder()
			.roomId(1L)
			.messages(messages)
			.build();


		given(projectChatRoomService.getChatRoom(anyLong(), anyLong(),anyLong()))
			.willReturn(dto);

		//when
		//then
		mockMvc.perform(get("/projects/1/chats/1"))
			.andDo(print())
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.roomId").value(dto.getRoomId()))
			.andExpect(jsonPath("$.messages[0].message").value(message.getMessage()))
			.andExpect(jsonPath("$.messages[0].nickname").value(message.getNickname()));

	}


}