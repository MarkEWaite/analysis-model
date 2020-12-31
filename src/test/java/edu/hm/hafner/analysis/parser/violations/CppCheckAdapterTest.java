package edu.hm.hafner.analysis.parser.violations;

import org.junit.jupiter.api.Test;

import edu.hm.hafner.analysis.AbstractParserTest;
import edu.hm.hafner.analysis.LineRange;
import edu.hm.hafner.analysis.LineRangeList;
import edu.hm.hafner.analysis.Report;
import edu.hm.hafner.analysis.Severity;
import edu.hm.hafner.analysis.assertions.SoftAssertions;

import static edu.hm.hafner.analysis.assertions.Assertions.*;

/**
 * Tests the class {@link CppCheckAdapter}.
 *
 * @author Ullrich Hafner
 */
class CppCheckAdapterTest extends AbstractParserTest {
    CppCheckAdapterTest() {
        super("cppcheck.xml");
    }

    @Override
    protected void assertThatIssuesArePresent(final Report report, final SoftAssertions softly) {
        softly.assertThat(report).hasSize(3);
        softly.assertThat(report.get(0))
                .hasMessage(
                        "The scope of the variable 'i' can be reduced. Warning: It can be unsafe to fix this message. Be careful. Especially when there are inner loops. Here is an example where cppcheck will write that the scope for 'i' can be reduced:\n"
                                + "void f(int x)\n"
                                + "{\n"
                                + "    int i = 0;\n"
                                + "    if (x) {\n"
                                + "        // it's safe to move 'int i = 0' here\n"
                                + "        for (int n = 0; n < 10; ++n) {\n"
                                + "            // it is possible but not safe to move 'int i = 0' here\n"
                                + "            do_something(&i);\n"
                                + "        }\n"
                                + "    }\n"
                                + "}\n"
                                + "When you see this message it is always safe to reduce the variable scope 1 level.")
                .hasFileName("api.c")
                .hasType("variableScope")
                .hasLineStart(498)
                .hasSeverity(Severity.WARNING_LOW);
        softly.assertThat(report.get(2))
                .hasMessage(
                        "The scope of the variable 'i' can be reduced. Warning: It can be unsafe to fix this message. Be careful. Especially when there are inner loops. Here is an example where cppcheck will write that the scope for 'i' can be reduced:\n"
                                + "void f(int x)\n"
                                + "{\n"
                                + "    int i = 0;\n"
                                + "    if (x) {\n"
                                + "        // it's safe to move 'int i = 0' here\n"
                                + "        for (int n = 0; n < 10; ++n) {\n"
                                + "            // it is possible but not safe to move 'int i = 0' here\n"
                                + "            do_something(&i);\n"
                                + "        }\n"
                                + "    }\n"
                                + "}\n"
                                + "When you see this message it is always safe to reduce the variable scope 1 level.")
                .hasFileName("api_storage.c")
                .hasType("variableScope")
                .hasLineStart(104)
                .hasSeverity(Severity.WARNING_HIGH);
    }

    /**
     * Verifies that the parser finds multiple locations (line ranges) for a given warning.
     *
     * @see <a href="https://issues.jenkins-ci.org/browse/JENKINS-55733">Issue 55733</a>
     */
    @Test
    void shouldFindMultipleLocations() {
        Report report = parse("issue55733.xml");

        assertThat(report).hasSize(2);

        assertThat(report.get(0)).hasFileName(
                "apps/cloud_composer/src/point_selectors/rectangular_frustum_selector.cpp")
                .hasLineStart(53)
                .hasMessage("Variable 'it' is reassigned a value before the old one has been used.")
                .hasType("redundantAssignment");
        assertThat(report.get(0).getLineRanges()).isEqualTo(new LineRangeList(new LineRange(51)));

        assertThat(report.get(1)).hasFileName(
                "surface/src/3rdparty/opennurbs/opennurbs_brep_tools.cpp")
                .hasLineStart(346)
                .hasMessage("Condition 'rc' is always true")
                .hasType("knownConditionTrueFalse");
        assertThat(report.get(1).getLineRanges()).isEqualTo(new LineRangeList(new LineRange(335)));
    }

    /**
     * Verifies that the parser finds multiple locations (line ranges) for a given warning with the same error ID.
     *
     * @see <a href="https://issues.jenkins-ci.org/browse/JENKINS-55733">Issue 55733</a>
     */
    @Test
    void shouldFindMultipleLocationsWithSameId() {
        Report report = parse("issue55733-multiple-tags-with-same-id.xml");

        assertThat(report).hasSize(2);

        assertThat(report.get(0))
                .hasFileName("apps/cloud_composer/src/point_selectors/rectangular_frustum_selector.cpp")
                .hasLineStart(53)
                .hasMessage("Variable 'it' is reassigned a value before the old one has been used.")
                .hasType("redundantAssignment");
        assertThat(report.get(0).getLineRanges()).isEqualTo(new LineRangeList(new LineRange(51)));

        assertThat(report.get(1))
                .hasFileName("that/cloud_composer/src/point_selectors/rectangular_frustum_selector.cpp")
                .hasLineStart(53)
                .hasMessage("Variable 'that' is reassigned a value before the old one has been used.")
                .hasType("redundantAssignment");
        assertThat(report.get(1).getLineRanges()).isEqualTo(new LineRangeList(new LineRange(51)));
    }

    /**
     * Verifies that the parser finds errors without location.
     *
     * @see <a href="https://issues.jenkins-ci.org/browse/JENKINS-64519">Issue 64519</a>
     */
    @Test
    void shouldFindErrorWithoutLocation() {
        Report report = parse("issue64519.xml");

        assertThat(report).hasSize(1);
    }

    @Override
    protected CppCheckAdapter createParser() {
        return new CppCheckAdapter();
    }
}
