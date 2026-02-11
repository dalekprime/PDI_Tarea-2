# **PDI_Tarea-2**

Este proyecto implementa una aplicación de escritorio robusta para el análisis, procesamiento y manipulación de imágenes digitales. Desarrollada en Kotlin utilizando JavaFX para la interfaz gráfica y OpenCV como motor de procesamiento matricial de alto rendimiento.

## Funcionalidades Principales

* Gestión de Archivos: Soporte estándar para carga y guardado de PNG, JPG y BMP. Implementación manual de lectores/escritores para formatos académicos Netpbm (PPM/PGM/PBM) y compresión RLE.

* Visualización y Navegación: Zoom con interpolación seleccionable (Vecino más próximo vs. Bilineal). Sistema de Panning con modos de Recorte (Mueve la ventana de visión), Expandir (visualización infinitacon bordes negros) y Llenado (expande físicamente la matriz de la imagen rellenando con negro). Análisis mediante Histograma RGB, Curva de Tono y Perfil de Línea.

* Preprocesamiento y Color: Operaciones puntuales de brillo, contraste y negativo. Ajuste en espacio HLS y Balance de Blancos en espacio YUV. Cuantización de color mediante reducción uniforme, K-Means y Popularidad.

* Filtros y Morfología: Filtros espaciales de suavizado, mediana y detección de bordes (Sobel, Prewitt, Roberts, Perfilado). Operaciones de morfología matemática (Erosión, Dilatación, Apertura, Cierre) con elementos estructurantes personalizables.

* Segmentación y Frecuencia: Umbralización simple, múltiple y automática (Otsu e Isodata). Crecimiento de regiones FloodFill con tolerancia de color y vecindad configurable (4 u 8). Dominio de frecuencia con visualización de espectro y filtros Paso Bajo (Ideal y Gaussiano).

## Decisiones de Diseño

* Arquitectura MVC: Se separó la lógica de negocio de la interfaz. BasicViewController actúa como Manejador Principal, mientras que controladores especializados encapsulan la lógica algorítmica. ImageMatrix funciona como wrapper del modelo, almacenando la imagen y su historial.

* Gestión de Memoria en Frecuencia: Para la Transformada de Fourier (DFT), se convierten implícitamente las imágenes a flotante (CV_32F).

## Librerías y Dependencias

* JDK 17+: Entorno de desarrollo Java.

* JavaFX: Interfaz gráfica de usuario.

* OpenCV 4.x: Biblioteca de visión por computador (con binarios nativos vinculados).

* Maven/Gradle: Gestión de construcción y dependencias.

# **Autores**

*Desarrollado para la cátedra de Procesamiento Digital de Imagenes. Universidad Central de Venezuela (UCV). Por Bryan Silva y Oriana Arellano, 2026.*