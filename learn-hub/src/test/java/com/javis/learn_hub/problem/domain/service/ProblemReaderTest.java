package com.javis.learn_hub.problem.domain.service;

import com.javis.learn_hub.member.domain.Member;
import com.javis.learn_hub.problem.domain.Problem;
import com.javis.learn_hub.support.TestFixtureFactory;
import com.javis.learn_hub.support.application.dto.CursorPageRequest;
import com.javis.learn_hub.support.application.dto.CursorSortDirection;
import com.javis.learn_hub.support.builder.MemberBuilder;
import com.javis.learn_hub.support.builder.ProblemBuilder;
import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ProblemReaderTest {

    private TestFixtureFactory testFixtureFactory = new TestFixtureFactory();
    private ProblemReader problemReader = new ProblemReader(
            testFixtureFactory.getProblemRepository(),
            testFixtureFactory.getProblemScoringInfoRepository()
    );

    @DisplayName("[최신순 조회 상황] 회원 id, 페이징 조건으로 회원이 작성한 모든 루트 문제를 최신순으로 가져온다.")
    @Test
    void testGetAllRootProblem() {
        //given
        Member writer = testFixtureFactory.make(MemberBuilder.builder().build());
        Problem oldProblem = testFixtureFactory.make(ProblemBuilder.builder().withWriterId(writer.getId()).build());
        Problem newProblem = testFixtureFactory.make(ProblemBuilder.builder().withWriterId(writer.getId()).build());
        List<Problem> expected = List.of(newProblem, oldProblem);
        CursorPageRequest cursorPageRequest = CursorPageRequest.builder().withSort(CursorSortDirection.DESC).build();

        //when
        List<Problem> actual = problemReader.getAllRootProblem(writer.getId(), cursorPageRequest);

        //then
        Assertions.assertThat(actual).containsExactlyElementsOf(expected);
    }

    @DisplayName("[오래된 순 조회 상황] 회원 id, 페이징 조건으로 회원이 작성한 모든 루트 문제를 오래된 순으로 가져온다.")
    @Test
    void testGetAllRootProblem2() {
        //given
        Member writer = testFixtureFactory.make(MemberBuilder.builder().build());
        Problem oldProblem = testFixtureFactory.make(ProblemBuilder.builder().withWriterId(writer.getId()).build());
        Problem newProblem = testFixtureFactory.make(ProblemBuilder.builder().withWriterId(writer.getId()).build());
        List<Problem> expected = List.of(oldProblem, newProblem);
        CursorPageRequest cursorPageRequest = CursorPageRequest.builder().withSort(CursorSortDirection.ASC).build();

        //when
        List<Problem> actual = problemReader.getAllRootProblem(writer.getId(), cursorPageRequest);

        //then
        Assertions.assertThat(actual).containsExactlyElementsOf(expected);
    }
}
