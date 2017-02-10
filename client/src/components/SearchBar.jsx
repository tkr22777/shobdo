import React, {Component} from 'react';
import RidmikParser from '../utils/RidmikParser.js';

class SearchBar extends Component {

    constructor(props) {
        super(props);
        this.state = {
            searchTerm: ''
        };

        this.parser = new RidmikParser();
        this.textBuffer = '';
    }

    onInputChange(e) {
        let bangla = this.parser.toBangla(this.textBuffer);
        this.setState({searchTerm: bangla});
        this.props.onSearchTermChange(this.state.searchTerm);
    }
    handleKeyDown(e) {
        const x = e.keyCode;

        if (x === 32 || (x > 64 && x < 91) || (x > 96 && x < 123)) {
            this.textBuffer += e.key;
        }
        else {
            if (x === 8 && this.textBuffer.length > 1) {
                this.textBuffer = this.textBuffer.substring(0, this.textBuffer.length - 1);
                return;
            }
            this.textBuffer = '';
        }
    }

    render() {
        return (
            <div>
                <input type="text"
                    value={this.state.searchTerm}
                    onChange={e => this.onInputChange(e)}
                    onKeyDown={e => this.handleKeyDown(e)}
                    // ref={(input) => { this.textInput = input; }}
                />
                <h5>{this.state.searchTerm}</h5>
            </div>
        );
    }
}

SearchBar.displayName = 'SearchBar';

export default SearchBar;
