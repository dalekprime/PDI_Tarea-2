# PDI_Tarea-2

Este proyecto implementa una aplicación de escritorio robusta para el análisis, procesamiento y manipulación de imágenes digitales. Desarrollada en Kotlin utilizando JavaFX para la interfaz gráfica y OpenCV como motor de procesamiento matricial de alto rendimiento.

## Funcionalidades Principales

* Gestión de Archivos: Soporte estándar para carga y guardado de PNG, JPG y BMP. Implementación manual de lectores/escritores para formatos académicos Netpbm (PPM/PGM) y compresión RLE.

* Visualización y Navegación: Zoom con interpolación seleccionable (Vecino más próximo vs. Bilineal). Sistema de Panning avanzado con modos de Recorte (mueve la ventanade visión sin alterar la imagen), Expandir (visualización infinitacon bordes negros) y Llenado (expande físicamente la matrizde la imagen rellenando con negro). Análisis mediante Histograma RGB, Curva de Tono y Perfil de Línea.

* Preprocesamiento y Color: Operaciones puntuales de brillo, contraste y negativo. Ajuste en espacio HLS y Balance de Blancos en espacio YUV. Cuantización de color mediante reducción uniforme, K-Means (Clustering) y Popularidad.

* Filtros y Morfología: Filtros espaciales de suavizado, mediana y detección de bordes (Sobel, Prewitt, Roberts, Perfilado). Operaciones de morfología matemática (Erosión, Dilatación, Apertura, Cierre) con elementos estructurantes personalizables.

* Segmentación y Frecuencia: Umbralización simple, múltiple y automática (Otsu e Isodata). Crecimiento de regiones FloodFill con tolerancia de color y vecindad configurable (4 u 8). Dominio de frecuencia con visualización de espectro y filtros Paso Bajo (Ideal y Gaussiano).

## Decisiones de Diseño

* Arquitectura MVC: Se separó la lógica de negocio de la interfaz. BasicViewController actúa como orquestador, mientras que controladores especializados encapsulan la lógica algorítmica. ImageMatrix funciona como wrapper del modelo, almacenando la imagen y su historial.* *BasicViewController:* Orquestador principal. Maneja eventos de la UI y delega tareas.

* Motor OpenCV: Se priorizó el uso de operaciones matriciales nativas en C++ sobre la manipulación de píxeles en Java. Esto garantiza un rendimiento superior en filtros y FFT. La conversión a JavaFX Image ocurre solo en la etapa final de renderizado.

* Gestión de Memoria en Frecuencia: Para la Transformada de Fourier (DFT), se convierten implícitamente las imágenes a flotante (CV_32F).

## Asunciones del Enunciado

* Se asume el origen de coordenadas en la esquina superior izquierda. Para operaciones de graficado (perfiles), se invierte visualmente el eje Y para coincidir con la intuición cartesiana.

* Se asume que OpenCV carga imágenes en formato BGR por defecto. El sistema mantiene este formato internamente para consistencia algorítmica y solo convierte a RGB para la visualización en pantalla.

* Se asume que para el filtrado en frecuencia, el componente DC debe estar centrado. Por ello, se implementa el intercambio de cuadrantes (Quadrant Swap) antes y después del filtro.

* Se asume que en la cuantización K-Means la calidad visual es prioritaria sobre la velocidad, permitiendo tiempos de ejecución mayores respecto a la cuantización uniforme.

## Librerías y Dependencias

* JDK 17+: Entorno de desarrollo Java.

* JavaFX: Interfaz gráfica de usuario.

* OpenCV 4.x: Biblioteca de visión por computador (con binarios nativos vinculados).

* Maven/Gradle: Gestión de construcción y dependencias.

# **Autores**

*Desarrollado para la cátedra de Procesamiento Digital de Imagenes. Universidad Central de Venezuela (UCV). Por Bryan Silva y Oriana Arellano, 2026.*