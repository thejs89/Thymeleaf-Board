/**
 * 게시판 파일 업로드 공통 JavaScript
 */
(function() {
  'use strict';

  /**
   * 파일 업로드 매니저 팩토리 함수
   * 각 페이지마다 독립적인 인스턴스를 생성
   */
  function createFileUploadManager() {
    return {
      ui: { files: [] },
      fileInput: null,
      select: null,
      form: null,
      items: null,
      board: null,

      /**
       * 초기화
       */
      init: function(fileInputId, selectId, formId, boardData, actionUrl) {
        this.fileInput = document.getElementById(fileInputId);
        this.select = document.getElementById(selectId);
        this.form = document.getElementById(formId);
        this.board = boardData || {};
        this.actionUrl = actionUrl;

        if (!this.fileInput || !this.select || !this.form) {
          console.error('파일 업로드 초기화 실패: 필수 요소를 찾을 수 없습니다.');
          return;
        }

        // Proxy를 사용하여 배열 변경 감지
        this.items = new Proxy(this.ui.files, {
          set: (target, prop, value) => {
            target[prop] = value;
            if (!isNaN(prop) || prop === 'length') {
              this.renderSelect(target);
            }
            return true;
          },
          deleteProperty: (target, prop) => {
            delete target[prop];
            this.renderSelect(target);
            return true;
          }
        });

        // 파일 선택 이벤트 리스너
        this.fileInput.addEventListener('change', (event) => this.handleFileChange(event));
        
        // custom-file-input 라벨 업데이트를 위한 이벤트 리스너
        this.fileInput.addEventListener('change', (event) => this.updateFileLabel(event));

        // 버튼 이벤트 리스너 등록
        this.attachEventListeners();

        // 초기 파일 로드
        this.initFiles();
      },

      /**
       * 이벤트 리스너 등록
       */
      attachEventListeners: function() {
        // 파일 선택 버튼
        const selectBtn = this.form.querySelector('[data-action="selectFile"]');
        if (selectBtn) {
          selectBtn.addEventListener('click', (e) => {
            e.preventDefault();
            this.selectFile(e);
          });
        }

        // 파일 삭제 버튼
        const deleteBtn = this.form.querySelector('[data-action="deleteFile"]');
        if (deleteBtn) {
          deleteBtn.addEventListener('click', (e) => {
            this.deleteFile(e);
          });
        }

        // 저장 버튼
        const saveBtn = this.form.querySelector('[data-action="save"]');
        if (saveBtn && this.actionUrl) {
          saveBtn.addEventListener('click', (e) => {
            this.save(e, this.actionUrl);
          });
        }
      },

      /**
       * 파일 변경 핸들러
       */
      handleFileChange: function(event) {
        const orgFiles = [...(this.fileInput.files || [])];
        const existingKeys = new Set(this.ui.files.map(f => f.key));

        const files = orgFiles.map((file) => {
          const fileKey = file.key || `new::${file.name}::${Date.now()}`;
          return {
            mode: "REG",
            key: fileKey,
            name: file.name,
            size: file.size,
            type: file.type,
            file: file
          };
        }).filter(({key}) => !existingKeys.has(key)); // 중복 제거

        this.setItems([...this.ui.files, ...files]);
        // 파일 입력 필드 리셋하여 같은 파일도 다시 선택 가능하도록
        this.fileInput.value = '';
      },

      /**
       * 파일 선택 라벨 업데이트
       */
      updateFileLabel: function(event) {
        const fileLabel = this.fileInput.nextElementSibling;
        if (fileLabel && fileLabel.classList.contains('custom-file-label')) {
          const fileCount = this.fileInput.files.length;
          if (fileCount > 0) {
            fileLabel.textContent = fileCount + '개의 파일이 선택되었습니다';
          } else {
            fileLabel.textContent = '파일을 선택하세요';
          }
        }
      },

      /**
       * 초기 파일 로드
       */
      initFiles: function() {
        const updFiles = [...(Array.isArray(this.board.file) ? this.board.file : [this.board.file].filter(v => !!v))]
          .map(({key, mode = "UPD", ...o}) => ({
            ...o,
            mode,
            key: key || String(o.seq || '')
          }));
        this.setItems(updFiles);
      },

      /**
       * 파일 선택 버튼 클릭
       */
      selectFile: function(e) {
        if (e && e.preventDefault) {
          try {
            e.preventDefault();
          } catch (e) {}
        }
        this.fileInput.click();
      },

      /**
       * Select 박스 렌더링
       */
      renderSelect: function(items) {
        this.select.innerHTML = "";
        items.forEach(item => {
          const option = document.createElement("option");
          option.value = item.key;
          let text = item.name;
          if (item.mode === "REG") {
            text = `${item.name} (새파일)`;
          } else if (item.mode === "DEL") {
            text = `${item.name} (삭제)`;
          }
          option.textContent = text;
          this.select.appendChild(option);
        });
      },

      /**
       * 아이템 설정
       */
      setItems: function(newArray) {
        this.items.length = 0;
        newArray.forEach(item => this.items.push(item));
      },

      /**
       * 파일 삭제
       */
      deleteFile: function(e) {
        e.preventDefault();

        const fileList = this.ui.files;
        const fileSelect = this.select;

        const selectedFiles = [...fileSelect.options]
          .filter(({selected = false}) => selected)
          .map(({value}) => fileList.find(({key}) => key === value));

        const updateFileList = fileList.map(o => {
          const isSelected = selectedFiles.findIndex(({key}) => o.key === key) >= 0;
          if (!isSelected) {
            return o;
          }
          // REG 모드인 경우 완전히 제거, UPD/DEL 모드인 경우 DEL로 변경
          if (o.mode === "REG") {
            return null; // 제거
          }
          return {...o, mode: "DEL"};
        }).filter(item => item !== null); // null 제거

        this.setItems(updateFileList);
      },

      /**
       * 폼 저장
       */
      save: function(e, actionUrl) {
        e.preventDefault();

        const uploadFiles = this.ui.files.filter(({mode}) => mode === "REG");
        const removeFiles = this.ui.files.filter(({mode}) => mode === "DEL");

        const formData = new FormData(this.form);

        const baseKey = {id: 'BOARD'};
        formData.append("baseFileInfo", JSON.stringify(baseKey));

        // upload
        const fileInfo = uploadFiles.map(({file, key, mode, name, size, type, ...o}, i) => {
          formData.append('file', file);
          return {...o};
        });
        formData.append('fileInfo', JSON.stringify(fileInfo));

        // remove
        const remove = {key: removeFiles.map(({key}) => key)};
        formData.append("removeFiles", JSON.stringify(remove));

        // 모든 응답을 JSON으로 처리
        fetch(actionUrl, {
          method: "POST",
          body: formData,
          headers: {
            'Accept': 'application/json'
          }
        })
        .then(response => {
          // 응답이 성공이든 실패든 JSON으로 파싱 시도
          return response.json().then(data => {
            // response.ok가 false이면 에러로 처리
            if (!response.ok) {
              return Promise.reject({ data, response });
            }
            return data;
          });
        })
        .then(data => {
          // 성공 응답 처리
          if (data.success && data.redirectUrl) {
            // 성공 응답 - redirectUrl로 이동
            window.location.href = data.redirectUrl;
          } else if (data.redirectUrl) {
            // redirectUrl만 있는 경우
            window.location.href = data.redirectUrl;
          }
        })
        .catch(error => {
          // 에러 응답 처리
          if (error.data) {
            // JSON 에러 응답인 경우
            const errorData = error.data;
            let errorMessage = errorData.error || errorData.message || '요청 처리 중 오류가 발생했습니다.';
            
            // 사용자 에러인 경우 fieldErrors 처리
            if (errorData.userError && errorData.fieldErrors) {
              const fieldErrorMessages = Object.entries(errorData.fieldErrors)
                .map(([field, message]) => `${field}: ${message}`)
                .join('\n');
              errorMessage = errorData.message + '\n\n' + fieldErrorMessages;
            }
            
            alert(errorMessage);
          } else {
            // JSON 파싱 실패 또는 네트워크 에러
            console.error('Error:', error);
            alert('요청 처리 중 오류가 발생했습니다.');
          }
        });
      }
    };
  }

  // 전역으로 팩토리 함수 노출
  window.createBoardFileUpload = createFileUploadManager;
})();
