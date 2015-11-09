package com.alee.laf.radiobutton;

import com.alee.extended.painter.AdaptivePainter;
import com.alee.extended.painter.Painter;

import javax.swing.*;
import java.awt.*;

/**
 * Simple RadioButtonPainter adapter class.
 * It is used to install simple non-specific painters into WebCheckBoxUI.
 *
 * @author Alexandr Zernov
 */

public class AdaptiveRadioButtonPainter<E extends JRadioButton, U extends WebRadioButtonUI> extends AdaptivePainter<E, U>
        implements RadioButtonPainter<E, U>
{
    /**
     * Constructs new AdaptiveCheckBoxPainter for the specified painter.
     *
     * @param painter painter to adapt
     */
    public AdaptiveRadioButtonPainter ( final Painter painter )
    {
        super ( painter );
    }

    @Override
    public Rectangle getIconRect ()
    {
        return null;
    }
}