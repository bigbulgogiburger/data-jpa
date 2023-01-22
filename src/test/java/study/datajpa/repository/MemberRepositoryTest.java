package study.datajpa.repository;

import org.assertj.core.api.Assert;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import study.datajpa.dto.MemberDto;
import study.datajpa.entity.Member;
import study.datajpa.entity.Team;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
@Rollback(value = false)
class MemberRepositoryTest {

    @Autowired MemberRepository memberRepository;
    @Autowired TeamRepository teamRepository;


    // 이렇게 해도 상관 없다.
    @Autowired MemberQueryRepository memberQueryRepository;

    @PersistenceContext
    EntityManager em;


    @Test
    public void testMember(){
        System.out.println("memberRepository = " + memberRepository);
        Member member = new Member("memberA");
        Member savedMember = memberRepository.save(member);
        Member findMember = memberRepository.findById(savedMember.getId()).get();
        assertThat(findMember.getId()).isEqualTo(savedMember.getId());
        assertThat(findMember.getUsername()).isEqualTo(savedMember.getUsername());
        assertThat(findMember).isEqualTo(savedMember);
    }

    @Test
    public void basicCRUD(){
        Member member1 = new Member("member1");
        Member member2 = new Member("member2");
        memberRepository.save(member1);
        memberRepository.save(member2);

        Member findMember1 = memberRepository.findById(member1.getId()).get();
        Member findMember2 = memberRepository.findById(member2.getId()).get();

        assertThat(findMember1).isEqualTo(member1);
        assertThat(findMember2).isEqualTo(member2);
        List<Member> all = memberRepository.findAll();
        assertThat(all.size()).isEqualTo(2);

        long count = memberRepository.count();
        assertThat(count).isEqualTo(2);

        memberRepository.delete(member1);
        memberRepository.delete(member2);
        long deleteCount = memberRepository.count();
        assertThat(deleteCount).isEqualTo(0);

    }

    @Test
    public void findByUsernameAndAgeGreaterThen(){
        Member m1= new Member("AAA",10);
        Member m2= new Member("AAA",20);

        memberRepository.save(m1);
        memberRepository.save(m2);

        List<Member> result = memberRepository.findByUsernameAndAgeGreaterThan("AAA", 15);
        assertThat(result.get(0).getUsername()).isEqualTo("AAA");
        assertThat(result.get(0).getAge()).isEqualTo(20);
        assertThat(result.size()).isEqualTo(1);
    }

    @Test
    public void findHelloBy(){
        List<Member> hello =memberRepository.findTop3HelloBy();
    }

    @Test
    public void testNamedQuery(){
        Member m1= new Member("BBB",10);
        Member m2= new Member("AAA",20);

        memberRepository.save(m1);
        memberRepository.save(m2);

        List<Member> aaa = memberRepository.findByUsername("AAA");
        Member member = aaa.get(0);
        assertThat(m2.getUsername()).isEqualTo(member.getUsername());
    }

    @Test
    public void testQuery(){
        Member m1= new Member("AAA",10);
        Member m2= new Member("BBB",20);

        memberRepository.save(m1);
        memberRepository.save(m2);
        List<Member> result = memberRepository.findUser("AAA", 10);
        assertThat(m1).isEqualTo(result.get(0));
    }

    @Test
    public  void findUsernameTEst(){
        Member m1= new Member("AAA",10);
        Member m2= new Member("BBB",20);

        memberRepository.save(m1);
        memberRepository.save(m2);

        List<String> usernameAll = memberRepository.findUsernameAll();
        usernameAll.stream().forEach(s-> System.out.println("s = " + s));
    }
    @Test
    public  void findMemberDto(){
        Team team = new Team("teamA");
        teamRepository.save(team);
        Member m1= new Member("AAA",10);
        m1.setTeam(team);

        memberRepository.save(m1);

        List<MemberDto> memberDtoList = memberRepository.findMemberDtoList();
        memberDtoList.forEach(s-> System.out.println("dto : "+s));

    }

    @Test
    public  void findByUsernameListTest(){
        Member m1= new Member("AAA",10);
        Member m2= new Member("BBB",20);

        memberRepository.save(m1);
        memberRepository.save(m2);

        List<Member> nameList = memberRepository.findByNames(Arrays.asList("AAA", "BBB"));

        nameList.forEach(s-> System.out.println("s = " + s));
    }

    @Test
    public void returnType(){
        Member m1= new Member("AAA",10);
        Member m2= new Member("BBB",20);

        memberRepository.save(m1);
        memberRepository.save(m2);


        // 없으면 size 0 인 컬렉션
        List<Member> aaa = memberRepository.findListByUsername("AAA");

        // 없으면 null(단건)
        Member aaa1 = memberRepository.findMemberByUsername("AAA");
        Optional<Member> aaa2 = memberRepository.findOptionalByUsername("AAA");
        System.out.println("aaa = " + aaa.getClass());
        System.out.println("aaa1 = " + aaa1.getClass());
        System.out.println("aaa2 = " + aaa2.getClass());
        System.out.println("aaa2 = " + aaa2.orElse(new Member("aa",1)));
    }


    @Test
    public void paging(){
        memberRepository.save(new Member("member1",10));
        memberRepository.save(new Member("member2",10));
        memberRepository.save(new Member("member3",10));
        memberRepository.save(new Member("member4",10));
        memberRepository.save(new Member("member5",10));

        PageRequest pageRequest = PageRequest.of(0, 3, Sort.by(Sort.Direction.DESC, "username"));

        int age =10;
        int offset = 0;
        int limit = 3;

        //slice는 page 토탈 카운트 계산 못함.
        Page<Member> page = memberRepository.findByAge(age,pageRequest);
        // page에 토탈 카운트 쿼리가 따로 있음

        Page<MemberDto> mappedMemberDto = page.map(member -> new MemberDto(member.getId(), member.getUsername(), null));
        List<Member> content = page.getContent();
//        long totalElements = page.getTotalElements();

        for (Member member : content) {
            System.out.println("member = " + member);
        }
//        System.out.println("totalElements = " + totalElements);

        assertThat(content.size()).isEqualTo(3);
//        assertThat(page.getTotalElements()).isEqualTo(5);
        assertThat(page.getNumber()).isEqualTo(0);
//        assertThat(page.getTotalPages()).isEqualTo(2);
        assertThat(page.isFirst()).isTrue();
        assertThat(page.hasNext()).isTrue();
    }

    @Test
    public void bulkUpdate(){
        memberRepository.save(new Member("member1",10));
        memberRepository.save(new Member("member2",19));
        memberRepository.save(new Member("member3",20));
        memberRepository.save(new Member("member4",21));
        memberRepository.save(new Member("member5",40));

        int resultCount = memberRepository.bulkAgePlus(20);
//        em.flush(); 플러시는 없어도 됨
//        em.clear(); clearAutomatically로 해ㅆㄷ

        Member member5 = memberRepository.findByUsername("member5").get(0);
        System.out.println("member5 = " + member5);
        assertThat(resultCount).isEqualTo(3);
    }

    @Test
    public void findMemeberLazy(){
        Team teamA = new Team("teamA");
        Team teamB = new Team("teamB");
        teamRepository.save(teamA);
        teamRepository.save(teamB);

        Member member1 = new Member("member1", 10, teamA);
        Member member2 = new Member("member2", 10, teamB);

        memberRepository.save(member1);
        memberRepository.save(member2);

        em.flush();
        em.clear();

        List<Member> members = memberRepository.findAll();
        for (Member member : members) {
            System.out.println("member = " + member.getUsername());
            System.out.println("member = " + member.getTeam().getName());
        }

    }

    @Test
    public void queryHint(){
        Member member1 = memberRepository.save(new Member("member1", 10));
        em.flush();
        em.clear();

        Member findMember = memberRepository.findReadOnlyByUsername("member1");
        findMember.setUsername("member2");

        em.flush();
    }

    @Test
    public void lock(){
        Member member1 = memberRepository.save(new Member("member1", 10));
        em.flush();
        em.clear();

        List<Member> result = memberRepository.findLockByUsername("member1");

        em.flush();
    }

    @Test
    public void callCustom(){
        List<Member> result = memberRepository.findMemberCustom();

    }

    @Test
    public void specBasic(){
        //given
        Team teamA = new Team("teamA");
        em.persist(teamA);

        Member m1 = new Member("m1", 0, teamA);

        Member m2 = new Member("m2", 0, teamA);

        em.persist(m1);
        em.persist(m2);

        Specification<Member> spec = MemberSpec.username("m1").and(MemberSpec.teamName("teamA"));
        List<Member> result = memberRepository.findAll(spec);

        Assertions.assertThat(result.size()).isEqualTo(1);
    }

    @Test
        public void queryByExample() {
        //given
        Team teamA = new Team("teamA");
        em.persist(teamA);

        Member m1 = new Member("m1", 0, teamA);

        Member m2 = new Member("m2", 0, teamA);

        em.persist(m1);
        em.persist(m2);

        em.flush();
        em.clear();

        //when

        //probe
        Member member = new Member("m1");
        Team team = new Team("teamA");
        member.setTeam(team);

        ExampleMatcher matcher = ExampleMatcher.matching().withIgnorePaths("age");
        Example<Member> example = Example.of(member,matcher);

        List<Member> result = memberRepository.findAll(example);

        assertThat(result.get(0).getUsername()).isEqualTo("m1");

    }

    @Test
    public void projections() {
        //given
        Team teamA = new Team("teamA");
        em.persist(teamA);

        Member m1 = new Member("m1", 0, teamA);

        Member m2 = new Member("m2", 0, teamA);

        em.persist(m1);
        em.persist(m2);

        em.flush();
        em.clear();

        //when
        List<UsernameOnlyDto> result = memberRepository.findProjectionsByUsername("m1", UsernameOnlyDto.class);
        System.out.println("result = " + result.get(0).getUsername());

    }

    @Test
    public void nativeQuery(){
        //given
        Team teamA = new Team("teamA");
        em.persist(teamA);

        Member m1 = new Member("m1", 0, teamA);

        Member m2 = new Member("m2", 0, teamA);

        em.persist(m1);
        em.persist(m2);

        em.flush();
        em.clear();

        Member nativeQuery = memberRepository.findByNativeQuery("m1");
        System.out.println("nativeQuery = " + nativeQuery);
    }

    @Test
    public void nativeQuery22(){
        //given
        Team teamA = new Team("teamA");
        em.persist(teamA);

        Member m1 = new Member("m1", 0, teamA);

        Member m2 = new Member("m2", 0, teamA);

        em.persist(m1);
        em.persist(m2);

        em.flush();
        em.clear();

        Page<MemberProjection> result = memberRepository.findbyNativeProjection(PageRequest.of(1, 10));

        List<MemberProjection> content = result.getContent();
        for (MemberProjection memberProjection : content) {
            System.out.println("memberProjection = " + memberProjection.getUsername());
            System.out.println("memberProjection = " + memberProjection.getTeamName());
            System.out.println("memberProjection = " + memberProjection.getId());
        }
    }



}