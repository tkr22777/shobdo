import React, {Component} from 'react';
import RidmikParser from '../utils/RidmikParser.js';

class SearchBar extends Component {

    constructor(props) {
        super(props);
        this.state = {
            searchTerm: ''
        };

        this.parser = new RidmikParser();
        console.info(this.parser.toBangla('amar'));
        this.writing = '';
        this.phLen = 0;
        this.banglaMode = true;
        this.ctrlPressed = false;
    }

    insertAtCareto(el, dele, value) {
        if (el.selectionStart || el.selectionStart === '0') {
            let start = el.selectionStart;
            let end = el.selectionEnd;
            let valStart = el.value.substring(0, start - dele);
            let valEnd = el.value.substring(end, el.value.length);
            el.value = valStart + value + valEnd;
            return (valStart + value).length;
        }
        else {
            el.value += value;
            return el.value.length;
        }
    }

    setCaretPositiono(ctrl, pos) {
        if (ctrl.setSelectionRange) {
            ctrl.focus();
            ctrl.setSelectionRange(pos, pos);
        }
        else if (ctrl.createTextRange) {
            let range = ctrl.createTextRange();
            range.collapse(true);
            range.moveEnd('character', pos);
            range.moveStart('character', pos);
            range.select();
        }
    }


    handleKeyPress(e) {
        let x = (window.event) ? e.keyCode : e.which;
        // console.log('pressed');

        if (this.ctrlPressed || !this.banglaMode) {
            x = -1;
        }

        if ((x > 64 && x < 91) || (x > 96 && x < 123)) {
            this.writing += String.fromCharCode(e.which);
            let bangla = this.parser.toBangla(this.writing);
            let p = this.insertAtCareto(this.textInput, this.phLen, bangla);
            this.setCaretPositiono(this.textInput, p);
            this.phLen = bangla.length;
            return false;
        }
        else {
            this.writing = '';
            this.phLen = 0;
        }
        return true;
    }

    handleKeyDown(e) {
        let x = (window.event) ? e.keyCode : e.which;
        if (x === 17) {
            this.ctrlPressed = true;
            // return true;
        }
        // for chrome, when control is pressed, other keys go through keydown
        // and for firefox it goes through both keydown & keypress
        if ((x === 109 || x === 77) && this.ctrlPressed) {
            this.banglaMode = !this.banglaMode;
            this.writing = '';
            this.phLen = 0;
        }
    }

    handleKeyUp(e) {
        let x = (window.event) ? e.keyCode : e.which;
        if (x === 17) {
            this.ctrlPressed = false;
        }
        else if (x === 8) { // for chrome, backspace is not in keypress event
            this.writing = '';
            this.phLen = 0;
        }
    }


    onInputChange(searchTerm) {
        this.setState({searchTerm});
        this.props.onSearchTermChange(searchTerm);
    }

    render() {
        return (
            <div>
                <input type="text"
                    // value={this.state.searchTerm}
                    onChange={e => this.onInputChange(e.target.value)}
                    onKeyPress={e => this.handleKeyPress(e)}
                    onKeyDown={e => this.handleKeyDown(e)}
                    onKeyUp={e => this.handleKeyUp(e)}
                    ref={(input) => { this.textInput = input; }}
                />
                <h5>{this.state.searchTerm}</h5>
            </div>
        );
    }
}

SearchBar.displayName = 'SearchBar';

export default SearchBar;
