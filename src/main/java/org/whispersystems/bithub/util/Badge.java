/**
 * Copyright (C) 2013 Open WhisperSystems
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.whispersystems.bithub.util;

import com.google.common.io.Resources;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class Badge {

  public static byte[] createFor(String price) throws IOException {
    byte[]        badgeBackground = Resources.toByteArray(Resources.getResource("assets/badge.png"));
    BufferedImage bufferedImage   = ImageIO.read(new ByteArrayInputStream(badgeBackground));
    Graphics2D    graphics        = bufferedImage.createGraphics();

    graphics.setFont(new Font("OpenSans", Font.PLAIN, 34));
    graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                              RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
    graphics.drawString(price + " USD", 86, 45);
    graphics.dispose();

    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    ImageIO.write(bufferedImage, "png", baos);

    return baos.toByteArray();
  }

  public static byte[] createSmallFor(String price) throws IOException {
    byte[]        badgeBackground = Resources.toByteArray(Resources.getResource("assets/badge-small.png"));
    BufferedImage bufferedImage   = ImageIO.read(new ByteArrayInputStream(badgeBackground));
    Graphics2D    graphics        = bufferedImage.createGraphics();

    graphics.setFont(new Font("OpenSans", Font.PLAIN, 9));
    graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                              RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
    graphics.drawString(price + " USD", 22, 14);
    graphics.dispose();

    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    ImageIO.write(bufferedImage, "png", baos);

    return baos.toByteArray();
  }

}
