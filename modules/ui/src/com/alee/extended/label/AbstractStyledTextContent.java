/*
 * This file is part of WebLookAndFeel library.
 *
 * WebLookAndFeel library is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * WebLookAndFeel library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with WebLookAndFeel library.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.alee.extended.label;

import com.alee.painter.decoration.IDecoration;
import com.alee.painter.decoration.content.AbstractTextContent;
import com.alee.utils.*;
import com.alee.utils.general.Pair;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Abstract implementation of styled text content
 *
 * @param <E> component type
 * @param <D> decoration type
 * @param <I> content type
 * @author Alexandr Zernov
 * @see <a href="https://github.com/mgarin/weblaf/wiki/How-to-use-WebStyledLabel">How to use WebStyledLabel</a>
 */

@SuppressWarnings ( "UnusedParameters" )
public abstract class AbstractStyledTextContent<E extends JComponent, D extends IDecoration<E, D>, I extends AbstractStyledTextContent<E, D, I>>
        extends AbstractTextContent<E, D, I>
{
    /**
     * todo 1. Implement minimum rows count
     * todo 2. Implement custom colors for custom style elements
     */

    /**
     * Whether or not should ignore style font color settings.
     */
    @XStreamAsAttribute
    protected Boolean ignoreStyleColors;

    /**
     * Script font ratio.
     */
    @XStreamAsAttribute
    protected Float scriptFontRatio;

    /**
     * Whether or not should keep hard line breaks.
     */
    @XStreamAsAttribute
    protected Boolean preserveLineBreaks;

    /**
     * Size of gaps between label rows.
     */
    @XStreamAsAttribute
    protected Integer rowGap;

    /**
     * Global text style.
     * It can be specified to add an additional {@link StyleRange} with range [0,text.length].
     */
    @XStreamAsAttribute
    protected String globalStyle;

    /**
     * Runtime variables.
     */
    protected transient List<TextRange> textRanges;

    @Override
    public void activate ( final E c, final D d )
    {
        // Performing default actions
        super.activate ( c, d );

        // Building initial text ranges
        buildTextRanges ( c, d );
    }

    @Override
    public void deactivate ( final E c, final D d )
    {
        // Clearing text ranges
        textRanges = null;

        // Performing default actions
        super.deactivate ( c, d );
    }

    /**
     * Returns whether or not ignore style font color settings.
     *
     * @param c painted component
     * @param d painted decoration state
     * @return true if ignore style font color settings, false otherwise
     */
    protected boolean isIgnoreColorSettings ( final E c, final D d )
    {
        return ignoreStyleColors != null && ignoreStyleColors;
    }

    /**
     * Returns script font ratio.
     *
     * @param c painted component
     * @param d painted decoration state
     * @return script font ratio
     */
    protected Float getScriptFontRatio ( final E c, final D d )
    {
        return scriptFontRatio != null ? scriptFontRatio : 1.5f;
    }

    /**
     * Returns whether hard breaks are preserved or not.
     *
     * @param c painted component
     * @param d painted decoration state
     * @return true if hard breaks are preserved, false otherwise
     */
    protected boolean isPreserveLineBreaks ( final E c, final D d )
    {
        return preserveLineBreaks == null || preserveLineBreaks;
    }

    /**
     * Returns text row gap.
     *
     * @param c painted component
     * @param d painted decoration state
     * @return text row gap
     */
    protected int getRowGap ( final E c, final D d )
    {
        return rowGap != null ? rowGap : 0;
    }

    /**
     * Returns global style range.
     *
     * @param plainText plain text
     * @param c         painted component
     * @param d         painted decoration state   @return text row gap
     * @return global style range
     */
    protected StyleRange getGlobalStyle ( final String plainText, final E c, final D d )
    {
        return !TextUtils.isEmpty ( globalStyle ) && plainText != null ?
                new StyleSettings ( 0, plainText.length (), globalStyle ).getStyleRange () : null;
    }

    /**
     * Returns list of text style ranges.
     *
     * @param c painted component
     * @param d painted decoration state
     * @return list of text style ranges
     */
    protected abstract List<StyleRange> getStyleRanges ( E c, D d );

    /**
     * Returns text wrapping type.
     *
     * @param c painted component
     * @param d painted decoration state
     * @return text wrapping type
     */
    protected abstract TextWrap getWrapType ( E c, D d );

    /**
     * Returns maximum rows count.
     *
     * @param c painted component
     * @param d painted decoration state
     * @return maximum rows count
     */
    protected abstract int getMaximumRows ( E c, D d );

    /**
     * Builds text ranges based on plain text and style ranges.
     *
     * @param c painted component
     * @param d painted decoration state
     */
    protected void buildTextRanges ( final E c, final D d )
    {
        // Retrieving plain text
        final String plainText = getText ( c, d );

        // Retrieving component style ranges
        List<StyleRange> styleRanges = getStyleRanges ( c, d );

        // Adding global style on top of them
        final StyleRange globalStyle = getGlobalStyle ( plainText, c, d );
        if ( globalStyle != null )
        {
            // We have to copy ranges list to avoid affecting the source list
            styleRanges = CollectionUtils.copy ( styleRanges );
            styleRanges.add ( 0, globalStyle );
        }

        textRanges = new TextRanges ( plainText, styleRanges ).getTextRanges ();
    }

    @Override
    protected void paintText ( final Graphics2D g2d, final Rectangle bounds, final E c, final D d )
    {
        if ( textRanges != null )
        {
            final int x = bounds.x;
            int y = bounds.y;
            final int rg = Math.max ( 0, getRowGap ( c, d ) );

            // Layout the text
            final List<StyledTextRow> rows = layout ( c, d, bounds );

            if ( !rows.isEmpty () )
            {
                // Calculating y-axis offset
                final int va = getVerticalAlignment ( c, d );
                if ( va != TOP )
                {
                    // Calculating total height
                    int th = -rg;
                    for ( final StyledTextRow row : rows )
                    {
                        th += row.height + rg;
                    }

                    if ( th < bounds.height )
                    {
                        switch ( va )
                        {
                            case CENTER:
                                y += ( bounds.height - th ) / 2;
                                break;

                            case BOTTOM:
                                y += bounds.height - th;
                                break;
                        }
                    }
                }

                final Pair<Integer, Integer> fs = getFontSize ( c, d );
                y += fs.getKey ();

                // Painting the text
                for ( int i = 0; i < rows.size (); i++ )
                {
                    final StyledTextRow row = rows.get ( i );
                    paintRow ( c, d, g2d, bounds, x, y, row, i == rows.size () - 1 );
                    y += row.height + rg;
                }
            }
        }
    }

    /**
     * Performs styled text layout.
     *
     * @param c      painted component
     * @param d      painted decoration state
     * @param bounds painting bounds
     * @return List of rows to paint
     */
    protected List<StyledTextRow> layout ( final E c, final D d, final Rectangle bounds )
    {
        final int endY = bounds.y + bounds.height;
        final int endX = bounds.x + bounds.width;

        final Pair<Integer, Integer> fs = getFontSize ( c, d );
        final int maxRowHeight = fs.getValue ();
        final int maxAscent = fs.getKey ();

        int x = bounds.x;
        int y = bounds.y + maxAscent;

        final int mnemonicIndex = getMnemonicIndex ( c, d );
        final int mr = getMaximumRows ( c, d );
        final int rg = getRowGap ( c, d );
        final TextWrap wt = getWrapType ( c, d );
        final boolean preserveLineBreaks = isPreserveLineBreaks ( c, d );

        Font font = c.getFont ();
        final int defaultFontSize = font.getSize ();

        int rowCount = 0;
        int charDisplayed = 0;
        int nextRowStartIndex = 0;
        StyledTextRow row = new StyledTextRow ( maxRowHeight );

        boolean readyToPaint = false;
        final List<StyledTextRow> rows = new ArrayList<StyledTextRow> ();

        // Painting the text
        for ( int i = 0; i < textRanges.size (); i++ )
        {
            final TextRange textRange = textRanges.get ( i );
            final StyleRange style = textRange.styleRange;

            // Updating font if need
            final int size = ( style != null && ( style.isSuperscript () || style.isSubscript () ) ) ?
                    Math.round ( ( float ) defaultFontSize / getScriptFontRatio ( c, d ) ) : defaultFontSize;

            font = c.getFont ();
            if ( style != null && ( ( style.getStyle () != -1 && font.getStyle () != style.getStyle () ) || font.getSize () != size ) )
            {
                font = FontUtils.getCachedDerivedFont ( font, style.getStyle () == -1 ? font.getStyle () : style.getStyle (), size );
            }
            final FontMetrics cfm = c.getFontMetrics ( font );

            if ( textRange.text.equals ( "\n" ) )
            {
                if ( wt == TextWrap.none || preserveLineBreaks )
                {
                    if ( row.isEmpty () )
                    {
                        row.append ( " ", null, cfm, -1, -1 );
                    }
                    i++;
                    charDisplayed += 1;
                    readyToPaint = true;
                }
            }
            else if ( nextRowStartIndex == 0 || nextRowStartIndex < textRange.text.length () )
            {
                String s = textRange.text.substring ( nextRowStartIndex, textRange.text.length () );


                int strWidth = cfm.stringWidth ( s );
                final int widthLeft = endX - x;

                if ( wt != TextWrap.none && widthLeft < strWidth && widthLeft >= 0 )
                {
                    if ( ( ( mr > 0 && rowCount < mr - 1 ) || mr <= 0 ) && y + maxRowHeight + Math.max ( 0, rg ) <= endY )
                    {
                        int availLength = s.length () * widthLeft / strWidth + 1; // Optimistic prognoses
                        int firstWordOffset = Math.max ( 0, TextUtils.findFirstWordFromIndex ( s, 0 ) );
                        int nextRowStartInSubString = 0;

                        do
                        {
                            final String subStringThisRow;
                            int lastInWordEndIndex;
                            if ( wt == TextWrap.word || wt == TextWrap.mixed )
                            {
                                final String subString = s.substring ( 0, Math.max ( 0, Math.min ( availLength, s.length () ) ) );

                                // Searching last word start
                                lastInWordEndIndex = TextUtils.findLastRowWordStartIndex ( subString.trim () );

                                // Only one word in row left
                                if ( lastInWordEndIndex < 0 )
                                {
                                    if ( wt == TextWrap.word )
                                    {
                                        if ( row.isEmpty () )
                                        {
                                            // Search last index of the first word end
                                            nextRowStartInSubString = firstWordOffset + TextUtils.findFirstRowWordEndIndex ( s.trim () );
                                        }

                                        break;
                                    }
                                    else
                                    {
                                        if ( row.isEmpty () )
                                        {
                                            lastInWordEndIndex = availLength - 1;
                                            firstWordOffset = 0;
                                        }
                                    }
                                }

                                nextRowStartInSubString = firstWordOffset + lastInWordEndIndex + 1;
                                subStringThisRow = subString.substring ( 0, Math.min ( nextRowStartInSubString, subString.length () ) );
                            }
                            else//if ( wt == WrapType.character )
                            {
                                subStringThisRow = s.substring ( 0, Math.max ( 0, Math.min ( availLength, s.length () ) ) );
                                nextRowStartInSubString = availLength;
                                firstWordOffset = 0;
                            }

                            strWidth = cfm.stringWidth ( subStringThisRow );
                            if ( strWidth > widthLeft )
                            {
                                // todo Try to optimize
                                availLength--;
                            }
                        }
                        while ( strWidth > widthLeft && availLength > 0 );

                        if ( nextRowStartInSubString > 0 && ( availLength > 0 || ( availLength <= 0 && row.isEmpty () ) ) )
                        {
                            // Extracting wrapped text fragment
                            s = s.substring ( 0, Math.min ( nextRowStartInSubString, s.length () ) );
                            strWidth = row.append ( s, style, cfm, charDisplayed, mnemonicIndex );
                            charDisplayed += s.length ();
                            nextRowStartIndex += nextRowStartInSubString;
                        }
                        else if ( row.isEmpty () )
                        {
                            // Skipping current text range
                            i++;
                            strWidth = row.append ( s, style, cfm, charDisplayed, mnemonicIndex );
                            charDisplayed += s.length ();
                            nextRowStartIndex = 0;
                        }
                        else
                        {
                            strWidth = 0;
                            nextRowStartIndex = 0;
                        }
                    }
                    else
                    {
                        strWidth = row.append ( s, style, cfm, charDisplayed, mnemonicIndex );
                        charDisplayed += s.length ();
                        nextRowStartIndex = 0;
                    }

                    readyToPaint = true;
                }
                else
                {
                    strWidth = row.append ( s, style, cfm, charDisplayed, mnemonicIndex );
                    charDisplayed += s.length ();
                    nextRowStartIndex = 0;
                }

                x += strWidth;
            }
            else
            {
                nextRowStartIndex = 0;
            }

            if ( readyToPaint && !row.isEmpty () )
            {
                rows.add ( row );

                rowCount++;
                row = new StyledTextRow ( maxRowHeight );
                readyToPaint = false;

                // Setting up next row
                y += maxRowHeight + Math.max ( 0, rg );
                x = bounds.x;
                i--;

                // Checking that row is last
                if ( y > endY || ( mr > 0 && rowCount >= mr - 1 ) )
                {
                    break;
                }
            }
        }

        // Painting last row if need
        if ( !row.isEmpty () )
        {
            rows.add ( row );
        }

        return rows;
    }

    /**
     * Paints single styled text row.
     *
     * @param c      painted component
     * @param d      painted decoration state
     * @param g2d    graphics context
     * @param bounds painting bounds
     * @param textX  text X coordinate
     * @param textY  text Y coordinate
     * @param row    painted row
     * @param isLast whether or not painted row is last
     */
    protected void paintRow ( final E c, final D d, final Graphics2D g2d, final Rectangle bounds, final int textX, final int textY,
                              final StyledTextRow row, final boolean isLast )
    {
        int horizontalAlignment = getHorizontalAlignment ( c, d );
        final boolean ltr = c.getComponentOrientation ().isLeftToRight ();
        if ( ( horizontalAlignment == SwingConstants.TRAILING && !ltr ) || ( horizontalAlignment == SwingConstants.LEADING && ltr ) )
        {
            horizontalAlignment = SwingConstants.LEFT;
        }
        else if ( ( horizontalAlignment == SwingConstants.LEADING && !ltr ) || ( horizontalAlignment == SwingConstants.TRAILING && ltr ) )
        {
            horizontalAlignment = SwingConstants.RIGHT;
        }

        int x = textX;

        if ( bounds.width > row.width )
        {
            switch ( horizontalAlignment )
            {
                case CENTER:
                    x += ( bounds.width - row.width ) / 2;
                    break;
                case RIGHT:
                    x += bounds.width - row.width;
            }
        }

        final Font font = c.getFont ();
        final int defaultFontSize = font.getSize ();
        final FontMetrics fm = c.getFontMetrics ( font );
        final TextWrap wt = getWrapType ( c, d );
        final boolean truncated = isTruncated ( c, d );
        int charDisplayed = 0;

        for ( final TextRange textRange : row.fragments )
        {
            final StyleRange style = textRange.getStyleRange ();

            // Updating font if need
            final int size = ( style != null && ( style.isSuperscript () || style.isSubscript () ) ) ?
                    Math.round ( ( float ) defaultFontSize / getScriptFontRatio ( c, d ) ) : defaultFontSize;

            Font cFont = c.getFont ();
            if ( style != null && ( ( style.getStyle () != -1 && cFont.getStyle () != style.getStyle () ) || cFont.getSize () != size ) )
            {
                cFont = FontUtils.getCachedDerivedFont ( cFont, style.getStyle () == -1 ? cFont.getStyle () : style.getStyle (), size );
            }
            final FontMetrics cfm = c.getFontMetrics ( cFont );

            int y = textY;
            String s = textRange.text;
            final int strWidth = cfm.stringWidth ( s );

            // Checking mnemonic
            int mneIndex = -1;
            if ( row.mnemonic >= 0 && row.mnemonic < charDisplayed + s.length () )
            {
                mneIndex = row.mnemonic - charDisplayed;
            }

            // Checking trim needs
            final int availableWidth = bounds.width + bounds.x - x;
            if ( truncated && availableWidth < strWidth &&
                    ( wt == TextWrap.none || wt == TextWrap.word || isLast ) )
            {
                // Clip string
                s = SwingUtilities.layoutCompoundLabel ( cfm, s, null, 0, horizontalAlignment, 0, 0,
                        new Rectangle ( x, y, availableWidth, bounds.height ), new Rectangle (), new Rectangle (), 0 );
            }

            // Starting of actual painting
            g2d.setFont ( cFont );

            if ( style != null )
            {
                if ( style.isSuperscript () )
                {
                    y -= fm.getHeight () - cfm.getHeight ();
                }
                else if ( style.isSubscript () )
                {
                    y += fm.getDescent () - cfm.getDescent ();
                }
            }

            if ( style != null && style.getBackground () != null )
            {
                g2d.setPaint ( style.getBackground () );
                g2d.fillRect ( x, y - cfm.getAscent (), strWidth, cfm.getAscent () + cfm.getDescent () );
            }

            final boolean useStyleForeground = style != null && !isIgnoreColorSettings ( c, d ) && style.getForeground () != null;
            final Color textColor = useStyleForeground ? style.getForeground () : getColor ( c, d );
            g2d.setPaint ( textColor );
            paintStyledTextFragment ( c, d, g2d, s, x, y, mneIndex, cfm, style, strWidth );

            x += strWidth;
            charDisplayed += s.length ();
        }
    }

    /**
     * Returns font max height and ascent.
     *
     * @param c painted component
     * @param d painted decoration state
     * @return font max height and ascent
     */
    protected Pair<Integer, Integer> getFontSize ( final E c, final D d )
    {
        final Font font = c.getFont ();
        final int defaultFontSize = font.getSize ();
        final FontMetrics fm = c.getFontMetrics ( font );

        int maxHeight = fm.getHeight ();
        int maxAscent = fm.getAscent ();

        for ( final TextRange textRange : textRanges )
        {
            final StyleRange style = textRange.styleRange;
            final int size = ( style != null && ( style.isSuperscript () || style.isSubscript () ) ) ?
                    Math.round ( ( float ) defaultFontSize / getScriptFontRatio ( c, d ) ) : defaultFontSize;

            Font cFont = font;
            if ( style != null && ( ( style.getStyle () != -1 && cFont.getStyle () != style.getStyle () ) || cFont.getSize () != size ) )
            {
                cFont = FontUtils.getCachedDerivedFont ( cFont, style.getStyle () == -1 ? cFont.getStyle () : style.getStyle (), size );
                final FontMetrics fm2 = c.getFontMetrics ( cFont );
                maxHeight = Math.max ( maxHeight, fm2.getHeight () );
                maxAscent = Math.max ( maxAscent, fm2.getAscent () );
            }
        }

        return new Pair<Integer, Integer> ( maxAscent, maxHeight );
    }

    /**
     * Actually paints styled text fragment.
     *
     * @param c        painted component
     * @param d        painted decoration state
     * @param g2d      graphics context
     * @param s        text fragment
     * @param x        text X coordinate
     * @param y        text Y coordinate
     * @param mneIndex index of mnemonic
     * @param fm       text fragment font metrics
     * @param style    style of text fragment
     * @param strWidth text fragment width
     */
    protected void paintStyledTextFragment ( final E c, final D d, final Graphics2D g2d, final String s, final int x, final int y,
                                             final int mneIndex, final FontMetrics fm, final StyleRange style, final int strWidth )
    {
        paintTextFragment ( c, d, g2d, s, x, y, mneIndex );

        if ( style != null )
        {
            // todo Separate all these implementations into special TextAccessory interface implementations
            // todo Make each accessory configurable to some extent, for example to provide its color
            if ( style.isStrikeThrough () )
            {
                final int lineY = y + ( fm.getDescent () - fm.getAscent () ) / 2;
                g2d.drawLine ( x, lineY, x + strWidth - 1, lineY );
            }
            if ( style.isDoubleStrikeThrough () )
            {
                final int lineY = y + ( fm.getDescent () - fm.getAscent () ) / 2;
                g2d.drawLine ( x, lineY - 1, x + strWidth - 1, lineY - 1 );
                g2d.drawLine ( x, lineY + 1, x + strWidth - 1, lineY + 1 );
            }
            if ( style.isUnderlined () )
            {
                final int lineY = y + 1;
                g2d.drawLine ( x, lineY, x + strWidth - 1, lineY );
            }
            if ( style.isWaved () )
            {
                // tood Remove hardcoded red as soon as accessories are available
                final Paint op = GraphicsUtils.setupPaint ( g2d, Color.RED );
                final int waveY = y + 1;
                for ( int waveX = x; waveX < x + strWidth; waveX += 4 )
                {
                    if ( waveX + 2 <= x + strWidth - 1 )
                    {
                        g2d.drawLine ( waveX, waveY, waveX + 1, waveY );
                    }
                    if ( waveX + 4 <= x + strWidth - 1 )
                    {
                        g2d.drawLine ( waveX + 2, waveY + 1, waveX + 3, waveY + 1 );
                    }
                }
                GraphicsUtils.restorePaint ( g2d, op );
            }
        }
    }

    @Override
    protected Dimension getPreferredTextSize ( final E c, final D d, final Dimension available )
    {
        final Dimension vSize = getPreferredStyledTextSize ( c, d, new Dimension ( Short.MAX_VALUE, Short.MAX_VALUE ) );
        final Dimension hSize = getPreferredStyledTextSize ( c, d, available );
        return SwingUtils.max ( vSize, hSize );
    }

    /**
     * Returns preferred styled text size.
     *
     * @param c         painted component
     * @param d         painted decoration state
     * @param available theoretically available space for this content
     * @return preferred styled text size
     */
    protected Dimension getPreferredStyledTextSize ( final E c, final D d, final Dimension available )
    {
        final Dimension ps = new Dimension ( 0, 0 );
        if ( textRanges != null )
        {
            final List<StyledTextRow> rows = layout ( c, d, new Rectangle ( 0, 0, available.width, available.height ) );
            if ( !rows.isEmpty () )
            {
                final int rg = Math.max ( 0, getRowGap ( c, d ) );
                ps.height -= rg;
                for ( final StyledTextRow row : rows )
                {
                    ps.width = Math.max ( ps.width, row.width );
                    ps.height += row.height + rg;
                }
            }
        }
        return ps;
    }

    @Override
    public I merge ( final I content )
    {
        super.merge ( content );
        ignoreStyleColors = content.isOverwrite () || content.ignoreStyleColors != null ? content.ignoreStyleColors : ignoreStyleColors;
        scriptFontRatio = content.isOverwrite () || content.scriptFontRatio != null ? content.scriptFontRatio : scriptFontRatio;
        preserveLineBreaks = content.isOverwrite () || content.preserveLineBreaks != null ? content.preserveLineBreaks : preserveLineBreaks;
        rowGap = content.isOverwrite () || content.rowGap != null ? content.rowGap : rowGap;
        globalStyle = content.isOverwrite () || content.globalStyle != null ? content.globalStyle : globalStyle;
        return ( I ) this;
    }
}