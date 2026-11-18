package com.ram.trabajos.dominio;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class VideoTest {

    @Test
    void instagram_comoLoCopiaElCelular() {
        assertThat(Video.limpiar("https://www.instagram.com/reel/DCxYz12AbC/?igsh=MWQ1ZGUxMzBkMA=="))
                .isEqualTo("https://www.instagram.com/reel/DCxYz12AbC/");
        assertThat(Video.limpiar("instagram.com/reels/DCxYz12AbC")).isEqualTo("https://www.instagram.com/reel/DCxYz12AbC/");
        assertThat(Video.limpiar("https://www.instagram.com/r.a.mantenimiento/reel/DCxYz12AbC/"))
                .isEqualTo("https://www.instagram.com/reel/DCxYz12AbC/");
        assertThat(Video.limpiar("https://instagram.com/p/C9k_Lm-0aBc/")).isEqualTo("https://www.instagram.com/p/C9k_Lm-0aBc/");
    }

    @Test
    void youtube_enSusDistintasFormas() {
        String esperado = "https://www.youtube.com/watch?v=dQw4w9WgXcQ";
        assertThat(Video.limpiar("https://www.youtube.com/watch?v=dQw4w9WgXcQ&t=30s")).isEqualTo(esperado);
        assertThat(Video.limpiar("https://m.youtube.com/watch?feature=share&v=dQw4w9WgXcQ")).isEqualTo(esperado);
        assertThat(Video.limpiar("https://youtu.be/dQw4w9WgXcQ?si=abc")).isEqualTo(esperado);
        assertThat(Video.limpiar("https://youtube.com/shorts/dQw4w9WgXcQ?feature=share")).isEqualTo(esperado);
    }

    @Test
    void vacioEsSinVideo() {
        assertThat(Video.limpiar(null)).isNull();
        assertThat(Video.limpiar("   ")).isNull();
    }

    @Test
    void otrosSitiosOLinksQueNoSonDeUnVideo_seRechazan() {
        assertThatThrownBy(() -> Video.limpiar("https://www.tiktok.com/@ram/video/123")).hasMessageContaining("Instagram");
        assertThatThrownBy(() -> Video.limpiar("https://www.instagram.com/r.a.mantenimiento/")).hasMessageContaining("Instagram");
        assertThatThrownBy(() -> Video.limpiar("https://www.youtube.com/@canal")).hasMessageContaining("YouTube");
        assertThatThrownBy(() -> Video.limpiar("https://instagram.com.sitio-falso.com/reel/DCxYz12AbC/"))
                .hasMessageContaining("Instagram");
        assertThatThrownBy(() -> Video.limpiar("javascript:alert(1)")).hasMessageContaining("Instagram");
    }
}
