package chocoteamteam.togather.repository.impl;

import chocoteamteam.togather.dto.ProjectCondition;
import chocoteamteam.togather.dto.queryDslSimpleDto.SimpleProjectDto;
import chocoteamteam.togather.entity.Member;
import chocoteamteam.togather.entity.Project;
import chocoteamteam.togather.entity.ProjectTechStack;
import chocoteamteam.togather.entity.TechStack;
import chocoteamteam.togather.repository.MemberRepository;
import chocoteamteam.togather.repository.ProjectRepository;
import chocoteamteam.togather.repository.ProjectTechStackRepository;
import chocoteamteam.togather.repository.TechStackRepository;
import chocoteamteam.togather.type.ProjectStatus;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.annotation.PostConstruct;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Import(QueryDslTestConfig.class)
@DataJpaTest
@ExtendWith(SpringExtension.class)
class QueryDslProjectRepositoryImplTest {
    @Autowired
    private QueryDslProjectRepositoryImpl queryDslProjectRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ProjectTechStackRepository projectTechStackRepository;

    @Autowired
    private TechStackRepository techStackRepository;

    @BeforeAll
    public void dataSetup() {
        Member aMember = memberRepository.save(Member.builder().email("www.a.com").nickname("aaaa name").profileImage("image").build());
        Member bMember = memberRepository.save(Member.builder().email("www.b.com").nickname("bbbb name").profileImage("image").build());
        Member cMember = memberRepository.save(Member.builder().email("www.c.com").nickname("cccc name").profileImage("image").build());

        TechStack react = techStackRepository.save(TechStack.builder().name("react").build());
        TechStack angular = techStackRepository.save(TechStack.builder().name("angular").build());
        TechStack spring = techStackRepository.save(TechStack.builder().name("spring").build());
        TechStack nodejs = techStackRepository.save(TechStack.builder().name("nodejs").build());
        TechStack php = techStackRepository.save(TechStack.builder().name("php").build());

        Project aProject = projectRepository.save(Project.builder().member(aMember)
                .title("aaaa title").content("aaaa content").status(ProjectStatus.RECRUITING).build());
        Project bProject = projectRepository.save(Project.builder().member(bMember)
                .title("bbbb title").content("bbbb content").status(ProjectStatus.RECRUITING).build());
        Project cProject = projectRepository.save(Project.builder().member(cMember)
                .title("cccc title").content("cccc content").status(ProjectStatus.COMPLETED).build());
        Project dProject = projectRepository.save(Project.builder().member(aMember)
                .title("dddd title").content("dddd content").status(ProjectStatus.COMPLETED).build());


        projectTechStackRepository.save(new ProjectTechStack(aProject, react))
                .setProject(aProject);
        projectTechStackRepository.save(new ProjectTechStack(aProject, spring))
                .setProject(aProject);
        projectTechStackRepository.save(new ProjectTechStack(aProject, nodejs))
                .setProject(aProject);

        projectTechStackRepository.save(new ProjectTechStack(bProject, react))
                .setProject(bProject);
        projectTechStackRepository.save(new ProjectTechStack(bProject, spring))
                .setProject(bProject);
        projectTechStackRepository.save(new ProjectTechStack(bProject, nodejs))
                .setProject(bProject);

        projectTechStackRepository.save(new ProjectTechStack(cProject, php))
                .setProject(cProject);
        projectTechStackRepository.save(new ProjectTechStack(cProject, react))
                .setProject(cProject);

        projectTechStackRepository.save(new ProjectTechStack(dProject, php))
                .setProject(dProject);
        projectTechStackRepository.save(new ProjectTechStack(dProject, angular))
                .setProject(dProject);

        projectRepository.save(aProject);
        projectRepository.save(bProject);
        projectRepository.save(cProject);
        projectRepository.save(dProject);

        System.out.println("-------------------- insert query close ----------------\n\n");
    }

    private final long TOTAL_DATA_SIZE = 4;

    @Test
    @DisplayName("아무 조건 없이 조회")
    void noOption_search_test() {
        //given
        ProjectCondition projectCondition = ProjectCondition.builder()
                .limit(10L)
                .build();
        //when
        List<SimpleProjectDto> result = queryDslProjectRepository.findAllOptionAndSearch(projectCondition);
        //then

        assertEquals(4, result.size());
    }

    @Test
    @DisplayName("조회 페이징 테스트")
    void noOption_paging_test() {
        //given
        long pageSize = 2L;
        int pageNumber = 1;

        ProjectCondition projectCondition = ProjectCondition.builder()
                .limit(pageSize)
                .pageNumber(pageNumber)
                .build();
        //when
        List<SimpleProjectDto> result = queryDslProjectRepository.findAllOptionAndSearch(projectCondition);
        //then

        assertEquals(2, result.size());

        // 0 page = [1L, 2L] , 1 page = [3L, 4L]
        assertEquals(3L, result.get(0).getId());
        assertEquals(4L, result.get(1).getId());
    }

    @Test
    @DisplayName("작성자 이름으로 검색")
    void author_search() {
        //given
        ProjectCondition projectCondition = ProjectCondition.builder()
                .limit(TOTAL_DATA_SIZE)
                .author("cc")
                .build();
        //when
        List<SimpleProjectDto> result = queryDslProjectRepository.findAllOptionAndSearch(projectCondition);
        //then

        assertTrue(result.size() > 0);
        for (SimpleProjectDto simpleProjectDto : result) {
            assertTrue(simpleProjectDto.getMember().getNickname().contains("cc"));
        }
    }

    @Test
    @DisplayName("제목으로 검색")
    void title_search() {
        //given
        ProjectCondition projectCondition = ProjectCondition.builder()
                .limit(TOTAL_DATA_SIZE)
                .title("dd")
                .build();
        //when
        List<SimpleProjectDto> result = queryDslProjectRepository.findAllOptionAndSearch(projectCondition);
        //then

        assertTrue(result.size() > 0);
        for (SimpleProjectDto simpleProjectDto : result) {
            assertTrue(simpleProjectDto.getTitle().contains("dd"));
        }
    }

    @Test
    @DisplayName("내용으로 검색")
    void content_search() {
        //given
        ProjectCondition projectCondition = ProjectCondition.builder()
                .limit(TOTAL_DATA_SIZE)
                .content("cccc")
                .build();
        //when
        List<SimpleProjectDto> result = queryDslProjectRepository.findAllOptionAndSearch(projectCondition);
        //then

        assertEquals(1, result.size());
    }


    @Test
    @DisplayName("프로젝트 상태로 필터링")
    void search_projectStatus() {
        //given
        ProjectCondition projectCondition = ProjectCondition.builder()
                .limit(TOTAL_DATA_SIZE)
                .projectStatus(ProjectStatus.RECRUITING)
                .build();
        //when
        List<SimpleProjectDto> result = queryDslProjectRepository.findAllOptionAndSearch(projectCondition);
        //then

        assertTrue(result.size() > 0);
        for (SimpleProjectDto simpleProjectDto : result) {
            assertEquals(ProjectStatus.RECRUITING, simpleProjectDto.getStatus());
        }
    }

    @Test
    @DisplayName("기술스택으로 조회")
    void search_skillStack() {
        //given
        TechStack react = techStackRepository.findById(1L).get();
        TechStack spring = techStackRepository.findById(3L).get();

        ProjectCondition projectCondition = ProjectCondition.builder()
                .limit(TOTAL_DATA_SIZE)
                .techStackIds(List.of(react.getId(), spring.getId()))
                .build();
        //when

        List<SimpleProjectDto> result = queryDslProjectRepository.findAllOptionAndSearch(projectCondition);
        //then

        assertEquals(3, result.size());

        for (SimpleProjectDto simpleProjectDto : result) {
            assertTrue(simpleProjectDto.getTechStacks().stream()
                    .anyMatch(techStack ->
                            techStack.getId().equals(react.getId()) ||
                                    techStack.getId().equals(spring.getId())));
        }
    }

    @Test
    @DisplayName("모든 조건 동시에 적용 조회 테스트")
    void all_condition_search() {
        //given
        TechStack react = techStackRepository.findById(1L).get();
        TechStack php = techStackRepository.findById(5L).get();

        ProjectCondition projectCondition = ProjectCondition.builder()
                .limit(TOTAL_DATA_SIZE)
                .techStackIds(List.of(react.getId(), php.getId()))
                .projectStatus(ProjectStatus.COMPLETED)
                .title("cccc")
                .build();
        //when

        List<SimpleProjectDto> result = queryDslProjectRepository.findAllOptionAndSearch(projectCondition);
        //then

        assertEquals(3, result.get(0).getId());
    }

}