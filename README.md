# icandoenglish

영어 단어 암기 및 복습을 위한 Android 앱 프로젝트입니다.

---

## 📦 프로젝트 개요
- **목적:** 영어 단어 암기, 복습, 예문 학습, 학습 진도 관리
- **아키텍처:** MVVM + Clean Architecture
- **주요 기술:** Kotlin, Android SDK, Room, Hilt, Jetpack, JUnit

---

## 🛠️ 개발/빌드 환경

| 항목                | 권장 버전           |
|---------------------|---------------------|
| **OS**              | macOS (Ventura 13+), Windows 10+, Linux (Ubuntu 20.04+) |
| **Android Studio**  | Hedgehog (2023.1.1) 이상 |
| **JDK**             | Java 17 (필수)      |
| **Gradle**          | 8.5 이상            |
| **Android Gradle Plugin** | 8.1.4 이상   |
| **Kotlin**          | 1.9.x               |

---

## ✅ 내 환경이 맞는지 확인하는 방법

### 1. JDK 버전 확인
```bash
java -version
```
- **결과 예시:**
  ```
  openjdk version "17.0.10" 2024-01-16
  ```
- **17이 아니면?** 아래 설치법 참고

### 2. Gradle 버전 확인
```bash
./gradlew --version
```
- **결과 예시:**
  ```
  Gradle 8.5
  ```
- **8.5 미만이면?** 아래 설치법 참고

### 3. Android Studio 버전 확인
- 메뉴: `Android Studio` → `About Android Studio`
- **2023.1.1 Hedgehog** 이상인지 확인

---

## 🚀 환경 맞추는 방법

### 1. JDK 17 설치
- **macOS:**
  ```bash
  brew install openjdk@17
  sudo ln -sfn /opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk /Library/Java/JavaVirtualMachines/openjdk-17.jdk
  export PATH="/opt/homebrew/opt/openjdk@17/bin:$PATH"
  export JAVA_HOME="/opt/homebrew/opt/openjdk@17"
  ```
- **Windows:**
  - [Adoptium Temurin 17 다운로드](https://adoptium.net/)
  - 설치 후 환경변수 JAVA_HOME 설정

### 2. Gradle Wrapper 사용 (권장)
- 프로젝트 폴더에서 아래 명령어로 빌드
  ```bash
  ./gradlew clean build
  ```
- **직접 설치 필요 없음!**

### 3. Android Studio 최신 버전 설치
- [공식 다운로드 페이지](https://developer.android.com/studio)
- 기존 버전 삭제 후 새 버전 설치

---

## 🏗️ 빌드 및 실행 방법

1. **Android Studio로 열기**
2. `File > Sync Project with Gradle Files` 클릭
3. `Build > Make Project` 또는 ▶️ Run 버튼 클릭
4. 에뮬레이터/실기기에서 앱 실행

---

## 🐞 자주 발생하는 문제 & 해결법

- **에러: AGP requires Java 17 but found Java 11**
  - JDK 17로 환경변수 또는 Android Studio Gradle JDK 설정
- **Gradle 버전 오류**
  - `gradle-wrapper.properties`의 버전을 8.5 이상으로 수정
- **빌드 산출물이 Git에 올라감**
  - `.gitignore`를 반드시 확인

---

## 📂 주요 폴더 구조

```
icandoenglish/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/na982/icandoenglish/...
│   │   │   ├── res/
│   │   │   └── AndroidManifest.xml
│   ├── build.gradle
├── build.gradle
├── gradle/
├── gradlew, gradlew.bat
├── settings.gradle
└── .gitignore
```

---

## 🙋‍♂️ 문의/기여
- 이 저장소는 영어 학습 앱 개발을 위한 오픈소스 프로젝트입니다.
- 이슈/PR/질문 환영합니다!