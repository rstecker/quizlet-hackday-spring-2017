package sixarmstudios.quizletcolors.ui.board;

import org.junit.Test;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.List;

import studioes.arm.six.partskit.BoardView;
import ui.Option;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by rebeccastecker on 11/22/17.
 */

public class BoardFragmentTest {
    @Test
    public void test_swap1_middle() {
        BoardFragment frag = new BoardFragment();
        List<Option> options = Arrays.asList(
                makeOption(0, "Colorado"),
                makeOption(1, "Washington"),
                makeOption(2, "Montana"),
                makeOption(3, "Arizona")
        );
        frag.mBoard = Mockito.mock(BoardView.class);
        when(frag.mBoard.getCurrentOptions()).thenReturn(Arrays.asList("Colorado", "Alaska","Washington","Montana"));
        frag.handleOptionUpdates(options);
        verify(frag.mBoard, times(1)).setOptions(Arrays.asList("Colorado", "Arizona","Washington","Montana"));
    }
    @Test
    public void test_swap1_end() {
        BoardFragment frag = new BoardFragment();
        List<Option> options = Arrays.asList(
                makeOption(0, "Colorado"),
                makeOption(1, "Washington"),
                makeOption(2, "Montana"),
                makeOption(3, "Arizona")
        );
        frag.mBoard = Mockito.mock(BoardView.class);
        when(frag.mBoard.getCurrentOptions()).thenReturn(Arrays.asList("Colorado","Washington","Montana", "Alaska"));
        frag.handleOptionUpdates(options);
        verify(frag.mBoard, times(1)).setOptions(Arrays.asList("Colorado","Washington","Montana", "Arizona"));
    }
    @Test
    public void test_no_change() {
        BoardFragment frag = new BoardFragment();
        List<Option> options = Arrays.asList(
                makeOption(0, "Colorado"),
                makeOption(1, "Washington"),
                makeOption(2, "Montana"),
                makeOption(3, "Arizona")
        );
        frag.mBoard = Mockito.mock(BoardView.class);
        when(frag.mBoard.getCurrentOptions()).thenReturn(Arrays.asList("Colorado", "Washington","Montana","Arizona"));
        frag.handleOptionUpdates(options);
        verify(frag.mBoard, times(1)).setOptions(Arrays.asList("Colorado", "Washington","Montana","Arizona"));
    }

    @Test
    public void test_only_reorder() {
        BoardFragment frag = new BoardFragment();
        List<Option> options = Arrays.asList(
                makeOption(0, "Colorado"),
                makeOption(1, "Washington"),
                makeOption(2, "Montana"),
                makeOption(3, "Arizona")
        );
        frag.mBoard = Mockito.mock(BoardView.class);
        when(frag.mBoard.getCurrentOptions()).thenReturn(Arrays.asList("Montana", "Arizona","Washington","Colorado"));
        frag.handleOptionUpdates(options);
        verify(frag.mBoard, times(1)).setOptions(Arrays.asList("Montana", "Arizona","Washington","Colorado"));
    }
    private Option makeOption(int index, String option) {
        Option o = new Option();
        o.option = option;
        o.index = index;
        return o;
    }
}
