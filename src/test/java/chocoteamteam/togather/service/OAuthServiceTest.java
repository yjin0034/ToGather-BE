package chocoteamteam.togather.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import chocoteamteam.togather.dto.SignUpControllerDto;
import chocoteamteam.togather.dto.SignUpServiceDto;
import chocoteamteam.togather.dto.SignUpTokenMemberInfo;
import chocoteamteam.togather.dto.Tokens;
import chocoteamteam.togather.entity.Member;
import chocoteamteam.togather.entity.MemberTechStack;
import chocoteamteam.togather.entity.TechStack;
import chocoteamteam.togather.exception.CustomOAuthException;
import chocoteamteam.togather.exception.ErrorCode;
import chocoteamteam.togather.exception.TechStackException;
import chocoteamteam.togather.repository.MemberRepository;
import chocoteamteam.togather.repository.MemberTechStackRepository;
import chocoteamteam.togather.repository.TechStackRepository;
import chocoteamteam.togather.type.MemberStatus;
import chocoteamteam.togather.type.ProviderType;
import chocoteamteam.togather.type.Role;
import chocoteamteam.togather.type.TechCategory;
import java.util.ArrayList;
import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith({MockitoExtension.class})
class OAuthServiceTest {

    @Mock
    JwtService jwtService;
    @Mock
    MemberRepository memberRepository;
    @Mock
    TechStackRepository techStackRepository;
    @Mock
    MemberTechStackRepository memberTechStackRepository;

    @InjectMocks
    OAuthService oAuthService;

    String signUpToken = "signToken";
    String nickname = "name";
    String profileImage = "image";
    String accessToken = "12345";
    String refreshToken = "54321";
    String email = "test@test.com";
    String provider = "GOOGLE";
    List<Long> techStackDtos = new ArrayList(List.of(1L));

    @DisplayName("회원가입 성공")
    @Test
    void signUp_success() {
        // given
        Member member = Member.builder()
            .id(1L)
            .email(email)
            .nickname("test")
            .profileImage(profileImage)
            .status(MemberStatus.PERMITTED)
            .role(Role.ROLE_USER)
            .providerType(ProviderType.GOOGLE)
            .build();
        TechStack techStack = TechStack.builder()
            .id(1L)
            .name("tech")
            .image("image")
            .category(TechCategory.BACKEND)
            .build();
        MemberTechStack memberTechStack = MemberTechStack.builder()
            .member(member)
            .techStack(techStack)
            .build();
        given(memberRepository.save(any())).willReturn(member);
        given(memberTechStackRepository.save(any())).willReturn(memberTechStack);
        given(techStackRepository.findAllById(any()))
            .willReturn(List.of(techStack));
        given(jwtService.parseSignUpToken(any())).willReturn(SignUpTokenMemberInfo.builder()
            .email(email)
            .provider(provider)
            .build());
        given(jwtService.issueTokens(any())).willReturn(Tokens.builder()
            .accessToken(accessToken)
            .refreshToken(refreshToken)
            .build());
        given(memberTechStackRepository.save(any())).willReturn(
            chocoteamteam.togather.entity.MemberTechStack.builder().build());

        // when
        SignUpControllerDto.Response response = oAuthService.signUp(
            SignUpServiceDto.builder()
                .signUpToken(signUpToken)
                .nickname(nickname)
                .profileImage(profileImage)
                .techStackDtoList(techStackDtos)
                .build());

        // then
        Assertions.assertThat(response.getId()).isEqualTo(1L);
        Assertions.assertThat(response.getProfileImage()).isEqualTo(profileImage);
        Assertions.assertThat(response.getTechStackDtos().get(0).getId())
            .isEqualTo(techStack.getId());
        Assertions.assertThat(response.getTechStackDtos().get(0).getImage())
            .isEqualTo(techStack.getImage());
        Assertions.assertThat(response.getTechStackDtos().get(0).getName())
            .isEqualTo(techStack.getName());
        Assertions.assertThat(response.getTechStackDtos().get(0).getCategory())
            .isEqualTo(techStack.getCategory());
        Assertions.assertThat(response.getAccessToken()).isEqualTo(accessToken);
        Assertions.assertThat(response.getRefreshToken()).isEqualTo(refreshToken);
    }

    @DisplayName("회원 가입 실패 - 기술이 존재하지 않음")
    @Test
    void signUp_fail_not_found_techStack() {
        // given
        given(jwtService.parseSignUpToken(any())).willReturn(SignUpTokenMemberInfo
            .builder()
            .email("test")
            .provider("GOOGLE")
            .build());
        given(memberRepository.save(any())).willReturn(Member.builder().build());

        // when

        // then
        assertThatThrownBy(
            () -> oAuthService.signUp(SignUpServiceDto.builder()
                .signUpToken(signUpToken)
                .nickname(nickname)
                .profileImage(profileImage)
                .techStackDtoList(techStackDtos)
                .build()))
            .isInstanceOf(TechStackException.class)
            .hasMessage(ErrorCode.NOT_FOUND_TECH_STACK.getErrorMessage());
    }

    @DisplayName("회원 가입 실패 - 이미 존재하는 닉네임")
    @Test
    void signUp_fail_exits_true_nickname() {
        // given
        given(memberRepository.existsByNickname(nickname)).willReturn(true);
        given(jwtService.parseSignUpToken(any())).willReturn(
            SignUpTokenMemberInfo
                .builder()
                .email(email)
                .provider(provider)
                .build()
        );

        // when

        // then
        assertThatThrownBy(
            () -> oAuthService.signUp(SignUpServiceDto.builder()
                .signUpToken(signUpToken)
                .nickname(nickname)
                .profileImage(profileImage)
                .techStackDtoList(techStackDtos)
                .build()))
            .isInstanceOf(CustomOAuthException.class)
            .hasMessage(ErrorCode.EXIST_TRUE_MEMBER_NICKNAME.getErrorMessage());
    }

}
